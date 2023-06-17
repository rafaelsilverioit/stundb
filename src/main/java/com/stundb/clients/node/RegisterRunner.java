package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.RegisterRequest;
import com.stundb.service.RegisterResponse;
import io.grpc.ManagedChannel;

public class RegisterRunner
        extends NodeRunner
        implements GrpcRunner<RegisterRequest, RegisterResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof RegisterRequest;
    }

    @Override
    public RegisterResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).register((RegisterRequest) request);
    }
}
