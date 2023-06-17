package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.PingRequest;
import com.stundb.service.PingResponse;
import io.grpc.ManagedChannel;

public class PingRunner
        extends NodeRunner
        implements GrpcRunner<PingRequest, PingResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof PingRequest;
    }

    @Override
    public PingResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).ping((PingRequest) request);
    }
}
