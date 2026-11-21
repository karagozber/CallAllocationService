package org.karagozber.callallocationservice.service;

import org.junit.jupiter.api.Test;
import org.karagozber.callallocationservice.entity.Call;
import org.karagozber.callallocationservice.entity.Node;
import org.karagozber.callallocationservice.exception.CallNotFoundException;
import org.karagozber.callallocationservice.exception.NoAvailableNodeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallAllocationServiceTest {

    private final CallAllocationService service = new CallAllocationService();
    private Node node;
    private  Call call;

    @Test
    void allocatesCallToRegisteredNodeInRequestedRegion() {
        node = new Node("node-eu-1", "eu-west", 100, 20);
        call = new Call("call-1", "eu-west");
        service.registerNode(node);

        String nodeId = service.allocateCall(call);

        assertThat(nodeId).isEqualTo("node-eu-1");
    }

    @Test
    void ignoresNodesOutsideRequestedRegion() {
        node = new Node("node-eu-1", "eu-east", 100, 0);
        call = new Call("call-1", "eu-west");
        service.registerNode(node);

        assertThatThrownBy(() -> service.allocateCall(call))
                .isInstanceOf(NoAvailableNodeException.class);
    }

    @Test
    void repeatedAllocationOfSameCallIdReturnsSameNode() {
        service.registerNode(new Node("node-eu-1", "eu-west", 100, 0));
        service.registerNode(new Node("node-eu-2", "eu-west", 100, 0));
        call = new Call("call-1", "eu-west");

        String firstNodeId = service.allocateCall(call);
        String secondNodeId = service.allocateCall(call);

        assertThat(secondNodeId).isEqualTo(firstNodeId);
    }

    @Test
    void picksLeastLoadedNodeInRegion() {
        service.registerNode(new Node("node-eu-busy", "eu-west", 100, 90));
        service.registerNode(new Node("node-eu-idle", "eu-west", 100, 10));

        String nodeId = service.allocateCall(new Call("call-1", "eu-west"));

        assertThat(nodeId).isEqualTo("node-eu-idle");
    }

    @Test
    void throwsWhenNoNodeHasFreeCapacityInRegion() {
        service.registerNode(new Node("node-eu-1", "eu-west", 10, 10));

        assertThatThrownBy(() -> service.allocateCall(new Call("call-1", "eu-west")))
                .isInstanceOf(NoAvailableNodeException.class);
    }

    @Test
    void endingACallFreesUpCapacityForFurtherAllocations() {
        service.registerNode(new Node("node-eu-1", "eu-west", 1, 0));
        service.allocateCall(new Call("call-1", "eu-west"));

        assertThatThrownBy(() -> service.allocateCall(new Call("call-2", "eu-west")))
                .isInstanceOf(NoAvailableNodeException.class);

        service.endCall("call-1");

        String nodeId = service.allocateCall(new Call("call-2", "eu-west"));
        assertThat(nodeId).isEqualTo("node-eu-1");
    }

    @Test
    void endingAnUnknownCallThrows() {
        assertThatThrownBy(() -> service.endCall("unknown-call"))
                .isInstanceOf(CallNotFoundException.class);
    }

    @Test
    void registeringAnExistingNodeIdUpdatesItsCapacityInfo() {
        service.registerNode(new Node("node-eu-1", "eu-west", 10, 10));
        assertThatThrownBy(() -> service.allocateCall(new Call("call-1", "eu-west")))
                .isInstanceOf(NoAvailableNodeException.class);

        service.registerNode(new Node("node-eu-1", "eu-west", 10, 0));

        String nodeId = service.allocateCall(new Call("call-1", "eu-west"));
        assertThat(nodeId).isEqualTo("node-eu-1");
    }
}
