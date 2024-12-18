# StunDB - A stunning distributed key-value database

----------------------------

## Disclaimer

This application is work in progress, so it is not ready for production yet.

----------------------------

## Running

### Jar

1. Download the runnable jar from the latest [release](https://github.com/rafaelsilverioit/stundb/releases)
2. In the same folder where the jar is placed, create a file named `application.yml` with contents similar to [this](https://github.com/rafaelsilverioit/stundb/blob/1.0.0/stundb/application/src/main/resources/application.yml)
3. Run the application with the following command: 
```bash
  java -jar stundb-application-1.0.0-shaded.jar -Dapplication.port=8000
```

### Maven

To run with Maven, you just need to run the following command:
```bash
  mvn exec:java -f stundb/application/pom.xml
```

----------------------------

## Backlog
### Done
- FIFO cache
- Locking
- Contact other nodes through seeds
- Replicate data (using CRDT)
- Leader election
- Use Netty for TCP server
- Implement node statuses so that when a node becomes unreachable, it is ignored until it becomes available again and state is synchronized (remove leader status too)
- Retry contacting seeds until a node replies, up to a maximum number of attempts
- Move from Kryo to Apache Fury
- Use a BTree to store data
- Support JPMS
- TTL/Cache invalidation
- Create a Spring Boot app to observe nodes and expose actions through a REST API
- 100% unit testing coverage for the application module
- Acceptance tests for critical journeys
- Use Netty for TCP client
- Authentication (SCRAM/SASL)

### Done & later removed
- Allow state persistence
- Use Java Sockets for TCP client

### Doing
- Deal with virtual clocks - Already have implemented an initial version clock, but still need to get rid of timestamps
- Change how synchronization works today - Initial change: nodes replicate changes to the leader node, then from time to time, nodes synchronize with the leader to sync their state with the entire cluster
- TESTS (unit & acceptance)!

### TODO
- TLS Encryption
- Support multiple cache eviction policies
- Create a node gateway app to receive requests from clients, and load balance requests between nodes
- Payload compression/decompression when talking between nodes
