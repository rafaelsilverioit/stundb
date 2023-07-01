package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.ElectedRunner;
import com.stundb.service.ElectedRequest;
import com.stundb.service.ElectedResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ElectedRunnerProvider implements Provider<GrpcRunner<ElectedRequest, ElectedResponse>> {

    @Inject
    private ElectedRunner runner;

    @Override
    public GrpcRunner<ElectedRequest, ElectedResponse> get() {
        return runner;
    }
}
