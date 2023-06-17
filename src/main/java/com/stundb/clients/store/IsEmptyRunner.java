package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.IsEmptyRequest;
import com.stundb.service.IsEmptyResponse;
import io.grpc.ManagedChannel;

public class IsEmptyRunner
        extends StoreRunner
        implements GrpcRunner<IsEmptyRequest, IsEmptyResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof IsEmptyRequest;
    }

    @Override
    public IsEmptyResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).isEmpty((IsEmptyRequest) request);
    }
}
