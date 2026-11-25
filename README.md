# Call Allocation Service

A simplified service that routes calls to conferencing nodes based on reported capacity.

## Running locally

### Prerequisites

- Java 25
- Docker (for container / Kubernetes steps)
- A Kubernetes cluster for the k8s steps (e.g. Docker Desktop's built-in Kubernetes)

### Option 1: Run with Maven

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080`.

### Option 2: Run with Docker

```bash
docker build -t callallocationservice:latest .
docker run -p 8080:8080 callallocationservice:latest
```

### Option 3: Run on Kubernetes

```bash
docker build -t callallocationservice:latest .
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

kubectl -n pexip port-forward svc/call-allocation-service 8080:80
```

> Note: the manifests reference the image as `callallocationservice:latest` with
> `imagePullPolicy: IfNotPresent`, so a local Docker build is picked up directly by
> single-node clusters sharing the same Docker daemon (e.g. Docker Desktop). For a
> remote cluster, push the image to a registry and update `k8s/deployment.yaml`
> accordingly.

### Example requests

Register (or update) a node:

```bash
curl -X POST localhost:8080/nodes \
  -H "Content-Type: application/json" \
  -d '{"id":"node-eu-1","region":"eu-west","capacity":100,"currentCalls":20}'
```

Allocate a call:

```bash
curl -X POST localhost:8080/calls \
  -H "Content-Type: application/json" \
  -d '{"callId":"call-1","region":"eu-west"}'
# {"nodeId":"node-eu-1"}
```

End a call:

```bash
curl -X DELETE localhost:8080/calls/call-1
```

### Running the tests

```bash
./mvnw test
```

## Design Notes

- All state (nodes, active calls) is held in memory in two
`HashMap`s guarded by a single `ReentrantLock`, matching the "single instance,
in-memory only" constraint.

- Among nodes in the requested region with
free capacity (`currentCalls < capacity`), the service picks the one with the lowest
`currentCalls / capacity` ratio. Trade-off: it requires scanning all nodes in a region on
every allocation, which is fine at the scale implied by this exercise but would need
an index at larger scale.

- Active calls are tracked in a `callId -> nodeId` map. A repeated
`allocateCall` for an already-active `callId` returns the previously assigned node
without re-evaluating load. Affinity is keyed on `callId` alone — the service assumes
a call's region does not change between allocations, which the spec implies but does
not state explicitly.

- `registerNode` overwrites the stored node
unconditionally with whatever `currentCalls`/`capacity` it reports. This means the
service's own bookkeeping (incrementing on allocate, decrementing on end) can be
superseded by the next heartbeat. This was a deliberate simplification: the node is
assumed to be the source of truth for its own load, and the service's local counters
are best-effort between heartbeats.

- `NoAvailableNodeException` (409 Conflict) when no node in the
region has free capacity, and `CallNotFoundException` (404 Not Found) when ending a
call that isn't active.

- I used IntelliJ and Claude Code(mostly for guiding purposes and k8s manifests).


## Future Work

- With a region adjacency table we can assign call to a node in another region if all nodes are full in the target 
region. Or we can implement a auto-scale feature that creates a new node if usage reaches to a certain percentage.

- A solution that can detect dead nodes (by checking their health endpoint after TTL) and remove it from the map.

- Usage of Redis and a DB can be considered if more than one instance is needed.
