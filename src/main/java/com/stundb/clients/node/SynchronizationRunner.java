package com.stundb.clients.node;

import com.google.protobuf.GeneratedMessageV3;
import com.stundb.clients.GrpcRunner;
import com.stundb.service.CRDTRequest;
import com.stundb.service.CRDTResponse;
import io.grpc.ManagedChannel;

// TODO: Rename this
public class SynchronizationRunner
        extends NodeRunner
        // TODO: perhaps rename req-res too
        implements GrpcRunner<CRDTRequest, CRDTResponse> {

    @Override
    public Boolean isSupported(Object request) {
        return request instanceof CRDTRequest;
    }

    @Override
    public CRDTResponse execute(ManagedChannel channel, GeneratedMessageV3 request) {
        return getStubFor(channel).synchronize((CRDTRequest) request);
    }
}
