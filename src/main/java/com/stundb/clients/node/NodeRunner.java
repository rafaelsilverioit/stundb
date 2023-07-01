package com.stundb.clients.node;

import com.stundb.clients.GrpcRunner;
import com.stundb.service.NodesServiceGrpc;
import io.grpc.ManagedChannel;

public abstract class NodeRunner<REQ, RES> extends GrpcRunner<REQ, RES> {

    protected NodesServiceGrpc.NodesServiceStub getStubFor(ManagedChannel channel) {
        return NodesServiceGrpc.newStub(channel);
    }
}
