package com.stundb.modules.providers.runners;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.RegisterRunner;
import com.stundb.service.RegisterRequest;
import com.stundb.service.RegisterResponse;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class RegisterRunnerProvider implements Provider<GrpcRunner<RegisterRequest, RegisterResponse>> {

    @Inject
    private RegisterRunner runner;

    @Override
    public GrpcRunner<RegisterRequest, RegisterResponse> get() {
        return runner;
    }
}
