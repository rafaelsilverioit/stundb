package com.stundb.net.client.modules.providers;

import com.stundb.core.models.ApplicationConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class ExecutorServiceProvider implements Provider<ExecutorService> {

    @Inject
    private ApplicationConfig config;

    @Override
    public ExecutorService get() {
        var executor = Executors.newFixedThreadPool(config.getTcpClient().threads());
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        return executor;
    }
}
