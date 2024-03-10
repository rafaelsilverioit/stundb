package com.stundb.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.api.mappers.ApplicationConfigMapper;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.providers.ApplicationConfigProvider;
import com.stundb.api.providers.ObjectMapperProvider;
import com.stundb.api.providers.ValidatorProvider;
import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.logging.RequestLogger;
import com.stundb.core.models.UniqueId;
import com.stundb.modules.providers.*;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.modules.providers.CodecProvider;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.*;
import com.stundb.service.impl.*;
import com.stundb.timers.CoordinatorTimerTask;
import jakarta.validation.Validator;

import java.security.MessageDigest;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        install(new ClientModule());

        bindInterceptor(
                Matchers.any(), Matchers.annotatedWith(Loggable.class), new RequestLogger());

        bind(new TypeLiteral<Cache<Object>>() {}).toProvider(PublicCacheProvider.class);
        bind(new TypeLiteral<Cache<Node>>() {}).toProvider(InternalCacheProvider.class);

        // bind(Yaml.class).toInstance(new Yaml());
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(ApplicationConfig.class)
                .toProvider(ApplicationConfigProvider.class)
                .in(Singleton.class);
        bind(Validator.class)
                .toProvider(ValidatorProvider.class)
                .in(Singleton.class);
        bind(ObjectMapper.class)
                .toProvider(ObjectMapperProvider.class)
                .in(Singleton.class);
        bind(MessageDigest.class).toProvider(MessageDigestProvider.class);
        bind(UniqueId.class).toProvider(UniqueIdProvider.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);
        bind(Codec.class).toProvider(CodecProvider.class);
        bind(Timer.class).toInstance(new Timer());

        bind(ElectionService.class).to(ElectionServiceImpl.class);
        bind(StoreService.class).to(StoreServiceImpl.class);
        bind(NodeService.class).to(NodeServiceImpl.class);
        bind(ReplicationService.class).to(ReplicationServiceImpl.class);
        bind(SeedService.class).to(SeedServiceImpl.class);

        bind(new TypeLiteral<List<? extends CommandHandler>>() {})
                .toProvider(CommandHandlerProvider.class);

        bind(TimerTask.class)
                .annotatedWith(Names.named("coordinatorTimerTask"))
                .to(CoordinatorTimerTask.class);
    }
}
