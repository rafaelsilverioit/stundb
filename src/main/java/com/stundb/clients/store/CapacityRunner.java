package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.CapacityRequest;
import com.stundb.service.CapacityResponse;
import io.grpc.ManagedChannel;

public class CapacityRunner
        extends StoreRunner
        implements GrpcRunner<CapacityRequest, CapacityResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof CapacityRequest;
    }

    @Override
    public CapacityResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).capacity((CapacityRequest) request);
    }
}
