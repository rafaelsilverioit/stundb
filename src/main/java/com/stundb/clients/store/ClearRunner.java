package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.ClearRequest;
import com.stundb.service.ClearResponse;
import io.grpc.ManagedChannel;

public class ClearRunner
        extends StoreRunner
        implements GrpcRunner<ClearRequest, ClearResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof ClearRequest;
    }

    @Override
    public ClearResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).clear((ClearRequest) request);
    }
}
