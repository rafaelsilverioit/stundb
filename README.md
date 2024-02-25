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

### Doing
- TESTS (unit & acceptance)!

### TODO
- Deal with virtual clocks
- TTL
- Payload compression/decompression when talking between nodes
- Authentication
- Support multiple cache eviction policies
- Change how synchronization works today
- Create a Spring Boot app to observe nodes and expose data through a REST API
- Create a node gateway app to receive requests from clients, and load balance requests between nodes
