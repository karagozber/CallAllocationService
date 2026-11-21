package org.karagozber.callallocationservice.controller;

import org.junit.jupiter.api.Test;
import org.karagozber.callallocationservice.entity.Call;
import org.karagozber.callallocationservice.entity.Node;
import org.karagozber.callallocationservice.exception.CallNotFoundException;
import org.karagozber.callallocationservice.exception.NoAvailableNodeException;
import org.karagozber.callallocationservice.service.CallAllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CallAllocationController.class)
class CallAllocationControllerTest {
    Node node = new Node("node-eu-1", "eu-west", 100, 20);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CallAllocationService service;

    @Test
    void registerNode_returnsOk() throws Exception {
        mockMvc.perform(post("/nodes")
                        .contentType("application/json")
                        .content("""
                                {"id":"node-eu-1","region":"eu-west","capacity":100,"currentCalls":20}
                                """))
                .andExpect(status().isOk());

        verify(service).registerNode(node);
    }

    @Test
    void registerNode_withBlankId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/nodes")
                        .contentType("application/json")
                        .content("""
                                {"id":"","region":"eu-west","capacity":100,"currentCalls":20}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allocateCall_returnsAllocatedNodeId() throws Exception {
        given(service.allocateCall(new Call("call-1", "eu-west"))).willReturn("node-eu-1");

        mockMvc.perform(post("/calls")
                        .contentType("application/json")
                        .content("""
                                {"callId":"call-1","region":"eu-west"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"nodeId":"node-eu-1"}
                        """));
    }

    @Test
    void allocateCall_whenNoNodeAvailable_returnsConflict() throws Exception {
        given(service.allocateCall(any())).willThrow(new NoAvailableNodeException("eu-west"));

        mockMvc.perform(post("/calls")
                        .contentType("application/json")
                        .content("""
                                {"callId":"call-1","region":"eu-west"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void endCall_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/calls/call-1"))
                .andExpect(status().isNoContent());

        verify(service).endCall("call-1");
    }

    @Test
    void endCall_whenCallUnknown_returnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new CallNotFoundException("call-1")).when(service).endCall("call-1");

        mockMvc.perform(delete("/calls/call-1"))
                .andExpect(status().isNotFound());
    }
}
