package com.stundb.net.client.modules;

import com.google.inject.AbstractModule;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.client.StunDBClientImpl;
import com.stundb.net.client.modules.providers.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).asEagerSingleton();
        bind(StunDBClient.class).to(StunDBClientImpl.class);
    }
}
