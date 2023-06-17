package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.DelRequest;
import com.stundb.service.DelResponse;
import io.grpc.ManagedChannel;

public class DelRunner
        extends StoreRunner
        implements GrpcRunner<DelRequest, DelResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof DelRequest;
    }

    @Override
    public DelResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).del((DelRequest) request);
    }
}
