package com.stundb.clients.node;

import com.stundb.service.CRDTRequest;
import com.stundb.service.CRDTResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

// TODO: Rename this
public class SynchronizationRunner
        // TODO: perhaps rename req-res too
        extends NodeRunner<CRDTRequest, CRDTResponse> {

    @Override
    public void execute(ManagedChannel channel, CRDTRequest request, StreamObserver<CRDTResponse> observer) {
        getStubFor(channel).synchronize(request, observer);
    }
}
