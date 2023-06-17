package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.StartElectionRequest;
import com.stundb.service.StartElectionResponse;
import io.grpc.ManagedChannel;

public class StartElectionRunner
        extends NodeRunner
        implements GrpcRunner<StartElectionRequest, StartElectionResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof StartElectionRequest;
    }

    @Override
    public StartElectionResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).startElection((StartElectionRequest) request);
    }
}
