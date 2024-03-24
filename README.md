# StunDB - A stunning distributed key-value database

## Work in progress - Not ready for production yet

### Done
- FIFO cache
- Locking
- Contact other nodes through seeds
- Replicate data (using CRDT)
- Leader election
- Use Netty for TCP server
- Use Java Sockets for TCP client
- Implement node statuses so that when a node becomes unreachable, it is ignored until it becomes available again and state is synchronized (remove leader status too)
- Retry contacting seeds until a node replies, up to a maximum number of attempts
- Move from Kryo to Apache Fury
- Use a BTree to store data
- Allow state persistence
- Support JPMS
- TTL/Cache invalidation
- Create a Spring Boot app to observe nodes and expose actions through a REST API

### Doing
- Deal with virtual clocks - Already have implemented an initial version clock, but still need to get rid of timestamps
- Change how synchronization works today - Initial change: nodes replicate changes to the leader node, then from time to time, nodes synchronize with the leader to sync their state with the entire cluster
- TESTS (unit & acceptance)!

### TODO
- Support multiple cache eviction policies
- Create a node gateway app to receive requests from clients, and load balance requests between nodes
- Authentication
- Payload compression/decompression when talking between nodes
