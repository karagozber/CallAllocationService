package org.karagozber.callallocationservice.service;

import org.karagozber.callallocationservice.entity.Call;
import org.karagozber.callallocationservice.entity.Node;
import org.karagozber.callallocationservice.exception.CallNotFoundException;
import org.karagozber.callallocationservice.exception.NoAvailableNodeException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CallAllocationService {
  private final ReentrantLock lock = new ReentrantLock();

  private final Map<String, Node> nodesById = new HashMap<>();
  private final Map<String, String> activeCalls = new HashMap<>();

  public void registerNode(Node node) {
    lock.lock();
    try {
      nodesById.put(node.id(), node);
    } finally {
      lock.unlock();
    }
  }

  public String allocateCall(Call call) {
    lock.lock();
    try {
      String existingNodeId = activeCalls.get(call.id());
      if (existingNodeId != null) {
        return existingNodeId;
      }

      Node bestNode =
          getBestNode(call.region()).orElseThrow(() -> new NoAvailableNodeException(call.region()));
      nodesById.put(bestNode.id(), withOneMoreCall(bestNode));
      activeCalls.put(call.id(), bestNode.id());

      return bestNode.id();
    } finally {
      lock.unlock();
    }
  }

  public void endCall(String callId) {
    lock.lock();
    try {
      String nodeId = activeCalls.remove(callId);
      if (nodeId == null) {
        throw new CallNotFoundException(callId);
      }
      nodesById.computeIfPresent(nodeId, (id, node) -> withOneLessCall(node));
    } finally {
      lock.unlock();
    }
  }

  private Optional<Node> getBestNode(String region) {
    return nodesById.values().stream()
        .filter(node -> node.region().equals(region))
        .filter(node -> node.currentCalls() < node.capacity())
        .min(Comparator.comparingDouble(this::getNodeUsage));
  }

  private double getNodeUsage(Node node) {
    return (double) node.currentCalls() / node.capacity();
  }

  private Node withOneMoreCall(Node node) {
    return new Node(node.id(), node.region(), node.capacity(), node.currentCalls() + 1);
  }

  private Node withOneLessCall(Node node) {
    return new Node(
        node.id(), node.region(), node.capacity(), Math.max(0, node.currentCalls() - 1));
  }
}
