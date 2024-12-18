package com.stundb.net.client.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.stundb.api.mappers.ApplicationConfigMapper;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.providers.ApplicationConfigProvider;
import com.stundb.api.providers.ObjectMapperProvider;
import com.stundb.api.providers.ValidatorProvider;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.client.StunDBClientImpl;
import com.stundb.net.client.modules.providers.ExecutorServiceProvider;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.modules.NetCoreModule;
import com.stundb.net.core.modules.providers.CodecProvider;

import jakarta.validation.Validator;

import java.util.concurrent.ExecutorService;

public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new NetCoreModule());

        bind(Codec.class).toProvider(CodecProvider.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);
        bind(ApplicationConfig.class)
                .toProvider(ApplicationConfigProvider.class)
                .in(Singleton.class);
        bind(Validator.class)
                .toProvider(ValidatorProvider.class)
                .in(Singleton.class);
        bind(ObjectMapper.class)
                .toProvider(ObjectMapperProvider.class)
                .in(Singleton.class);
        bind(ExecutorService.class)
                .toProvider(ExecutorServiceProvider.class)
                .asEagerSingleton();
        bind(StunDBClient.class)
                .to(StunDBClientImpl.class)
                .asEagerSingleton();
    }
}
