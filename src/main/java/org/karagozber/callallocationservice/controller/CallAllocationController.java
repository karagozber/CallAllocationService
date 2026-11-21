package org.karagozber.callallocationservice.controller;

import jakarta.validation.Valid;
import org.karagozber.callallocationservice.dto.AllocateCallRequest;
import org.karagozber.callallocationservice.dto.AllocateCallResponse;
import org.karagozber.callallocationservice.entity.Call;
import org.karagozber.callallocationservice.entity.Node;
import org.karagozber.callallocationservice.service.CallAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class CallAllocationController {

    private final CallAllocationService callAllocationService;

    public CallAllocationController(CallAllocationService callAllocationService) {
        this.callAllocationService = callAllocationService;
    }

    @PostMapping("/calls")
    public ResponseEntity<AllocateCallResponse> allocateCall(@Valid @RequestBody AllocateCallRequest call) {
        String nodeId = callAllocationService.allocateCall(new Call(call.callId(), call.region()));
        return ResponseEntity.ok(new AllocateCallResponse(nodeId));
    }

    @PostMapping("/nodes")
    public ResponseEntity<Void> registerNode(@Valid @RequestBody Node node) {
        callAllocationService.registerNode(node);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/calls/{callId}")
    public ResponseEntity<Void> endCall(@PathVariable String callId) {
        callAllocationService.endCall(callId);
        return ResponseEntity.ok().build();
    }
}
