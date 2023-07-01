package com.stundb.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.stundb.cache.Cache;
import com.stundb.clients.GrpcRunner;
import com.stundb.configuration.ConfigurationLoader;
import com.stundb.logging.Loggable;
import com.stundb.logging.RequestLogger;
import com.stundb.mappers.ApplicationConfigMapper;
import com.stundb.models.ApplicationConfig;
import com.stundb.models.UniqueId;
import com.stundb.modules.providers.ApplicationConfigProvider;
import com.stundb.modules.providers.CacheProvider;
import com.stundb.modules.providers.MessageDigestProvider;
import com.stundb.modules.providers.UniqueIdProvider;
import com.stundb.modules.providers.runners.*;
import com.stundb.service.*;
import com.stundb.timers.CoordinatorTimerTask;
import org.yaml.snakeyaml.Yaml;

import java.security.MessageDigest;
import java.util.TimerTask;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(Loggable.class),
                new RequestLogger());

        bind(NodesServiceGrpc.NodesServiceImplBase.class)
                .to(NodesServiceImpl.class)
                .in(Scopes.SINGLETON);

        bind(StoreServiceGrpc.StoreServiceImplBase.class)
                .to(StoreServiceImpl.class)
                .in(Scopes.SINGLETON);

        bind(new TypeLiteral<Cache<String>>() {}).toProvider(new TypeLiteral<CacheProvider<String>>() {});
        bind(new TypeLiteral<Cache<Node>>() {}).toProvider(new TypeLiteral<CacheProvider<Node>>() {});

        bind(Yaml.class).toInstance(new Yaml());
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(ApplicationConfig.class).toProvider(ApplicationConfigProvider.class);
        bind(MessageDigest.class).toProvider(MessageDigestProvider.class);
        bind(UniqueId.class).toProvider(UniqueIdProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);

        bind(SyncService.class)
                .to(ReplicationService.class)
                .in(Scopes.SINGLETON);

        bind(AsyncService.class)
                .annotatedWith(Names.named("electionService"))
                .to(ElectionService.class);

        bind(TimerTask.class)
                .annotatedWith(Names.named("coordinatorTimerTask"))
                .to(CoordinatorTimerTask.class);

        bind(new TypeLiteral<GrpcRunner<ElectedRequest, ElectedResponse>>() {})
                .toProvider(ElectedRunnerProvider.class);

        bind(new TypeLiteral<GrpcRunner<ListNodesRequest, ListNodesResponse>>() {})
                .toProvider(ListNodesRunnerProvider.class);

        bind(new TypeLiteral<GrpcRunner<PingRequest, PingResponse>>() {})
                .toProvider(PingRunnerProvider.class);

        bind(new TypeLiteral<GrpcRunner<RegisterRequest, RegisterResponse>>() {})
                .toProvider(RegisterRunnerProvider.class);

        bind(new TypeLiteral<GrpcRunner<StartElectionRequest, StartElectionResponse>>() {})
                .toProvider(StartElectionRunnerProvider.class);

        bind(new TypeLiteral<GrpcRunner<CRDTRequest, CRDTResponse>>() {})
                .toProvider(SynchronizationRunnerProvider.class);

    }
}
