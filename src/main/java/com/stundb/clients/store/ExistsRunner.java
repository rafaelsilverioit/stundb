package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.ExistsRequest;
import com.stundb.service.ExistsResponse;
import io.grpc.ManagedChannel;

public class ExistsRunner
        extends StoreRunner
        implements GrpcRunner<ExistsRequest, ExistsResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof ExistsRequest;
    }

    @Override
    public ExistsResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).exists((ExistsRequest) request);
    }
}
