package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.ListNodesRunner;
import com.stundb.service.ListNodesRequest;
import com.stundb.service.ListNodesResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ListNodesRunnerProvider implements Provider<GrpcRunner<ListNodesRequest, ListNodesResponse>> {

    @Inject
    private ListNodesRunner runner;

    @Override
    public GrpcRunner<ListNodesRequest, ListNodesResponse> get() {
        return runner;
    }
}
