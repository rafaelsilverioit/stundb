package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.ElectedRequest;
import com.stundb.service.ElectedResponse;
import io.grpc.ManagedChannel;

public class ElectedRunner
        extends NodeRunner
        implements GrpcRunner<ElectedRequest, ElectedResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof ElectedRequest;
    }

    @Override
    public ElectedResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).elected((ElectedRequest) request);
    }
}
