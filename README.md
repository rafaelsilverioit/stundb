# StunDB - A stunning distributed key-value database

## Work in progress - Not ready for production yet

### Removed
- GRPC interface
- Async stubs for runners
- ~~Rename SyncService to something else (do we really need it?)~~ - we didn't

### Done
- FIFO cache
- Locking
- Contact other nodes through seeds
- Replicate data (using CRDT)
- Leader election
- Use Netty for TCP server
- Use Java Sockets for TCP client

### Doing
- Implement node statuses so that when a node becomes unreachable, it is ignored until it becomes available again and state is synchronized (remove leader status too)
- TESTS (unit & acceptance)!

### TODO
- Retry contacting seeds until a node is retrieved - or should we just fail to initialize?
- LBing - Consistent hashing
- TTL
- Payload compression/decompression when talking between nodes
- Authentication
- Support multiple cache eviction policies
- Change how synchronization works today


