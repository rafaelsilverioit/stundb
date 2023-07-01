package com.stundb.clients.node;

import com.stundb.service.ListNodesRequest;
import com.stundb.service.ListNodesResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class ListNodesRunner extends NodeRunner<ListNodesRequest, ListNodesResponse> {

    @Override
    public void execute(ManagedChannel channel, ListNodesRequest request, StreamObserver<ListNodesResponse> observer) {
        getStubFor(channel).list(request, observer);
    }
}
