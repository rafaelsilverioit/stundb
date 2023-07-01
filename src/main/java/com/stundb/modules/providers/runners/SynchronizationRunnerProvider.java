package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.SynchronizationRunner;
import com.stundb.service.CRDTRequest;
import com.stundb.service.CRDTResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class SynchronizationRunnerProvider implements Provider<GrpcRunner<CRDTRequest, CRDTResponse>> {

    @Inject
    private SynchronizationRunner runner;

    @Override
    public GrpcRunner<CRDTRequest, CRDTResponse> get() {
        return runner;
    }
}
