package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.PingRunner;
import com.stundb.service.PingRequest;
import com.stundb.service.PingResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class PingRunnerProvider implements Provider<GrpcRunner<PingRequest, PingResponse>> {

    @Inject
    private PingRunner runner;

    @Override
    public GrpcRunner<PingRequest, PingResponse> get() {
        return runner;
    }
}
