# StunDB - A stunning distributed key-value database

## Done
- FIFO cache
- Locking
- GRPC interface
- Contact other nodes through seeds
- Replicate data (using CRDT)
- Leader election

## TODO
- Rename SyncService to something else (do we really need it?)
- Retry contacting seeds until a node is retrieved
- Implement node statuses so that when a node becomes unreacheable, it is ignored until it becomes available again and state is synchronized (remove leader status too)
- Use async stubs for runners
- LBing - Consistent hashing
- TTL
- Support multiple cache eviction policies
- TESTS (unit & acceptance)!

