package com.stundb.clients.node;

import com.stundb.service.ElectedRequest;
import com.stundb.service.ElectedResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class ElectedRunner extends NodeRunner<ElectedRequest, ElectedResponse> {

    @Override
    public void execute(ManagedChannel channel, ElectedRequest request, StreamObserver<ElectedResponse> observer) {
        getStubFor(channel).elected(request, observer);
    }
}
