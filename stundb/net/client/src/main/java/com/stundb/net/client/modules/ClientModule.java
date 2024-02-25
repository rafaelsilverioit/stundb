package com.stundb.net.client.modules;

import com.google.inject.AbstractModule;
import com.stundb.core.mappers.ApplicationConfigMapper;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.modules.providers.ApplicationConfigProvider;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.client.StunDBClientImpl;
import com.stundb.net.client.modules.providers.ExecutorServiceProvider;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.modules.providers.CodecProvider;

import java.util.concurrent.ExecutorService;

public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Codec.class).toProvider(CodecProvider.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);
        bind(ApplicationConfig.class).toProvider(ApplicationConfigProvider.class);
        bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).asEagerSingleton();
        bind(StunDBClient.class).to(StunDBClientImpl.class);
    }
}
