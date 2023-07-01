package com.stundb.clients.node;

import com.stundb.service.PingRequest;
import com.stundb.service.PingResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class PingRunner extends NodeRunner<PingRequest, PingResponse> {

    @Override
    public void execute(ManagedChannel channel, PingRequest request, StreamObserver<PingResponse> observer) {
        getStubFor(channel).ping(request, observer);
    }
}
