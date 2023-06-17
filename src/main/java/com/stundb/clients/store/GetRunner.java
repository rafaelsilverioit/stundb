package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.GetRequest;
import com.stundb.service.GetResponse;
import io.grpc.ManagedChannel;

public class GetRunner
        extends StoreRunner
        implements GrpcRunner<GetRequest, GetResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof GetRequest;
    }

    @Override
    public GetResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).get((GetRequest) request);
    }
}
