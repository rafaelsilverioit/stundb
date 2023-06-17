package com.stundb.clients.store;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.SetRequest;
import com.stundb.service.SetResponse;
import io.grpc.ManagedChannel;

public class SetRunner
        extends StoreRunner
        implements GrpcRunner<SetRequest, SetResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof SetRequest;
    }

    @Override
    public SetResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).set((SetRequest) request);
    }
}
