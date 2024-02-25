package com.stundb.net.client.modules.providers;

import com.stundb.core.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class ExecutorServiceProvider implements Provider<ExecutorService> {

    @Inject private ApplicationConfig config;

    @Override
    public ExecutorService get() {
        var executor = Executors.newFixedThreadPool(config.getExecutors().tcpClient().threads());
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        return executor;
    }
}
