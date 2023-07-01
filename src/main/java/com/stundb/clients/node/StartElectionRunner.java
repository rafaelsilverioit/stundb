package com.stundb.clients.node;

import com.stundb.service.StartElectionRequest;
import com.stundb.service.StartElectionResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class StartElectionRunner extends NodeRunner<StartElectionRequest, StartElectionResponse> {

    @Override
    public void execute(ManagedChannel channel, StartElectionRequest request, StreamObserver<StartElectionResponse> observer) {
        getStubFor(channel).startElection(request, observer);
    }
}
