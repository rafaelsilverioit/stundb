package com.stundb.modules;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.stundb.core.cache.Cache;
import com.stundb.core.codecs.Codec;
import com.stundb.core.configuration.ConfigurationLoader;
import com.stundb.core.logging.Loggable;
import com.stundb.core.logging.RequestLogger;
import com.stundb.core.mappers.ApplicationConfigMapper;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.models.Node;
import com.stundb.core.models.UniqueId;
import com.stundb.core.modules.providers.ApplicationConfigProvider;
import com.stundb.modules.providers.CacheProvider;
import com.stundb.modules.providers.CommandHandlerProvider;
import com.stundb.modules.providers.MessageDigestProvider;
import com.stundb.modules.providers.UniqueIdProvider;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.modules.providers.CodecProvider;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.ElectionService;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import com.stundb.service.StoreService;
import com.stundb.service.impl.ElectionServiceImpl;
import com.stundb.service.impl.NodeServiceImpl;
import com.stundb.service.impl.ReplicationServiceImpl;
import com.stundb.service.impl.StoreServiceImpl;
import com.stundb.timers.CoordinatorTimerTask;
import org.yaml.snakeyaml.Yaml;

import java.security.MessageDigest;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        install(new ClientModule());

        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(Loggable.class),
                new RequestLogger());

        bind(new TypeLiteral<Cache<Object>>() {}).toProvider(new TypeLiteral<CacheProvider<Object>>() {});
        bind(new TypeLiteral<Cache<Node>>() {}).toProvider(new TypeLiteral<CacheProvider<Node>>() {});

        bind(Yaml.class).toInstance(new Yaml());
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(ApplicationConfig.class).toProvider(ApplicationConfigProvider.class);
        bind(MessageDigest.class).toProvider(MessageDigestProvider.class);
        bind(UniqueId.class).toProvider(UniqueIdProvider.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);
        bind(Codec.class).toProvider(CodecProvider.class);
        bind(Timer.class).toInstance(new Timer());

        bind(ElectionService.class).to(ElectionServiceImpl.class);
        bind(StoreService.class).to(StoreServiceImpl.class);
        bind(NodeService.class).to(NodeServiceImpl.class);
        bind(ReplicationService.class).to(ReplicationServiceImpl.class);
        bind(new TypeLiteral<List<? extends CommandHandler>>() {}).toProvider(CommandHandlerProvider.class);

        bind(TimerTask.class)
                .annotatedWith(Names.named("coordinatorTimerTask"))
                .to(CoordinatorTimerTask.class);
    }
}
