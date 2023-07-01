package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.StartElectionRunner;
import com.stundb.service.StartElectionRequest;
import com.stundb.service.StartElectionResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class StartElectionRunnerProvider implements Provider<GrpcRunner<StartElectionRequest, StartElectionResponse>> {

    @Inject
    private StartElectionRunner runner;

    @Override
    public GrpcRunner<StartElectionRequest, StartElectionResponse> get() {
        return runner;
    }
}
