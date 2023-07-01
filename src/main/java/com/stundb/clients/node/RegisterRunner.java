package com.stundb.clients.node;

import com.stundb.service.RegisterRequest;
import com.stundb.service.RegisterResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class RegisterRunner extends NodeRunner<RegisterRequest, RegisterResponse> {

    @Override
    public void execute(ManagedChannel channel, RegisterRequest request, StreamObserver<RegisterResponse> observer) {
        getStubFor(channel).register(request, observer);
    }
}
