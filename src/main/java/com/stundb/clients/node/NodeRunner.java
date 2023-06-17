package com.stundb.clients.node;

import com.stundb.service.NodesServiceGrpc;
import io.grpc.ManagedChannel;

public class NodeRunner {

    protected NodesServiceGrpc.NodesServiceBlockingStub getStubFor(ManagedChannel channel) {
        return NodesServiceGrpc.newBlockingStub(channel);
    }
}
