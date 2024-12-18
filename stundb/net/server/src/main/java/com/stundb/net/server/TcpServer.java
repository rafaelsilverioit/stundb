package com.stundb.net.server;

import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.codecs.ObjectDecoder;
import com.stundb.net.core.codecs.ObjectEncoder;
import com.stundb.net.core.managers.RequestManager;
import com.stundb.net.core.managers.SessionManager;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.security.auth.credentials.CredentialManager;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.net.server.handlers.DefaultCommandHandler;
import com.stundb.net.server.handlers.RequestHandler;
import com.stundb.net.server.handlers.SaslServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public abstract class TcpServer {

    @Inject protected StunDBClient client;
    @Inject protected ApplicationConfig config;
    @Inject private Codec codec;
    @Inject private Cache<Node> internalCache;
    @Inject private List<? extends CommandHandler> runners;
    @Inject private DefaultCommandHandler defaultCommandHandler;
    @Inject private UniqueId uniqueId;
    @Inject private CredentialManager credentialManager;
    @Inject private SessionManager sessionManager;
    @Inject private RequestManager requestManager;

    protected abstract void onStart();

    protected abstract void contactSeeds();

    public void run() {
        var executor = Executors.newFixedThreadPool(config.executors().initializer().threads());
        var mainGroup = new NioEventLoopGroup(config.executors().mainServerLoop().threads());
        var secondaryGroup =
                new NioEventLoopGroup(config.executors().secondaryServerLoop().threads());
        var handler =
                new RequestHandler(runners, defaultCommandHandler, sessionManager, requestManager);
        var bootstrap = new ServerBootstrap();
        bootstrap
                .group(mainGroup, secondaryGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer(handler))
                .bind(new InetSocketAddress(config.port()))
                .addListener(onStartListener(executor));
    }

    @Loggable
    private ChannelInitializer<Channel> channelInitializer(RequestHandler handler) {
        return new ChannelInitializer<>() {
            @Loggable
            @Override
            protected void initChannel(Channel channel) {
                var pipeline = channel.pipeline();
                var timeouts = config.timeouts();
                var tcpReadTimeout = timeouts.tcpReadTimeout();
                var tcpWriteTimeout = timeouts.tcpWriteTimeout();

                pipeline.addFirst(new LoggingHandler(LogLevel.INFO));
                pipeline.addFirst(new ReadTimeoutHandler(tcpReadTimeout, TimeUnit.SECONDS));
                pipeline.addFirst(new WriteTimeoutHandler(tcpWriteTimeout, TimeUnit.SECONDS));
                pipeline.addFirst(
                        "idleStateHandler",
                        new IdleStateHandler(
                                tcpReadTimeout, tcpWriteTimeout, tcpReadTimeout, TimeUnit.SECONDS));
                pipeline.addLast("decoder", new ObjectDecoder(codec));
                pipeline.addLast("encoder", new ObjectEncoder(codec));
                pipeline.addLast("sasl", new SaslServerHandler(credentialManager, sessionManager));
                pipeline.addLast("handler", handler);
                log.debug("----> [{}] connected!", channel.id());
            }
        };
    }

    private ChannelFutureListener onStartListener(ExecutorService executor) {
        return channelFuture -> {
            if (!seedsExcludingCurrentNode().isEmpty()) {
                CompletableFuture.runAsync(this::contactSeeds, executor);
            }

            startServer(channelFuture);
            executor.shutdown();
        };
    }

    private void startServer(ChannelFuture channelFuture) {
        if (!channelFuture.isSuccess()) {
            log.error("Server failed to start", channelFuture.cause());
            System.exit(1);
        }

        var myself =
                new Node(
                        config.ip(),
                        config.port(),
                        uniqueId.number(),
                        false,
                        com.stundb.net.core.models.NodeStatus.create(RUNNING));
        internalCache.upsert(uniqueId.text(), myself);

        onStart();
    }

    private List<String> seedsExcludingCurrentNode() {
        return config.seeds().stream().filter(seed -> !seed.equals(serverAddress())).toList();
    }

    protected String serverAddress() {
        return config.ip() + ":" + config.port();
    }
}
