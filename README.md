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
- TTL/Cache eviction
- Create a Spring Boot app to observe nodes and expose actions through a REST API

### Doing
- TESTS (unit & acceptance)!

### TODO
- Deal with virtual clocks
- Support multiple cache eviction policies
- Change how synchronization works today
- Create a node gateway app to receive requests from clients, and load balance requests between nodes
- Authentication
- Payload compression/decompression when talking between nodes
