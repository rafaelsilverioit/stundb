package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.ListNodesRequest;
import com.stundb.service.ListNodesResponse;
import io.grpc.ManagedChannel;

public class ListNodesRunner
        extends NodeRunner
        implements GrpcRunner<ListNodesRequest, ListNodesResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof ListNodesRequest;
    }

    @Override
    public ListNodesResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).list((ListNodesRequest) request);
    }
}
