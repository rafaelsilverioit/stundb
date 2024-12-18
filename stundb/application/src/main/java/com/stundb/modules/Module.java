package com.stundb.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.stundb.annotations.CacheEvictor;
import com.stundb.annotations.Coordinator;
import com.stundb.api.btree.BTree;
import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.core.cache.Cache;
import com.stundb.core.crdt.CRDT;
import com.stundb.core.crdt.LastWriterWinsSet;
import com.stundb.core.logging.Loggable;
import com.stundb.core.logging.RequestLogger;
import com.stundb.core.models.UniqueId;
import com.stundb.modules.providers.*;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.modules.providers.CodecProvider;
import com.stundb.net.server.TcpServer;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.server.TcpServerImpl;
import com.stundb.service.*;
import com.stundb.service.impl.*;
import com.stundb.timers.BackoffTimerTask;
import com.stundb.timers.impl.BackoffTimerTaskImpl;
import com.stundb.timers.impl.CacheEvictorTimerTaskImpl;
import com.stundb.timers.impl.CoordinatorTimerTaskImpl;

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

        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(MessageDigest.class).toProvider(MessageDigestProvider.class);
        bind(UniqueId.class).toProvider(UniqueIdProvider.class);
        bind(Codec.class).toProvider(CodecProvider.class);
        bind(Timer.class).toProvider(TimerProvider.class);
        bind(CRDT.class).to(LastWriterWinsSet.class).in(Singleton.class);
        bind(ElectionService.class).to(ElectionServiceImpl.class);
        bind(StoreService.class).to(StoreServiceImpl.class);
        bind(NodeService.class).to(NodeServiceImpl.class);
        bind(ReplicationService.class).to(ReplicationServiceImpl.class);
        bind(SeedService.class).to(SeedServiceImpl.class);
        bind(TcpServer.class).to(TcpServerImpl.class);

        bind(TimerTask.class)
                .annotatedWith(Coordinator.class)
                .to(CoordinatorTimerTaskImpl.class);
        bind(TimerTask.class)
                .annotatedWith(CacheEvictor.class)
                .to(CacheEvictorTimerTaskImpl.class);

        bind(BackoffTimerTask.class).to(BackoffTimerTaskImpl.class);

        bind(new TypeLiteral<List<? extends CommandHandler>>() {})
                .toProvider(CommandHandlerProvider.class);

        bind(new TypeLiteral<BTree<String, Object>>() {})
                .toProvider(new TypeLiteral<BTreeProvider<Object>>() {})
                .in(Singleton.class);

        bind(new TypeLiteral<BTree<String, Node>>() {})
                .toProvider(new TypeLiteral<BTreeProvider<Node>>() {})
                .in(Singleton.class);

        bind(new TypeLiteral<Cache<Object>>() {})
                .toProvider(new TypeLiteral<CacheProvider<Object>>() {})
                .in(Singleton.class);

        bind(new TypeLiteral<Cache<Node>>() {})
                .toProvider(new TypeLiteral<CacheProvider<Node>>() {})
                .in(Singleton.class);
    }
}
