package com.stundb.net.client;

import static java.util.Optional.ofNullable;

import com.stundb.net.client.handlers.ClientHandler;
import com.stundb.net.client.handlers.SaslClientHandler;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.codecs.ObjectDecoder;
import com.stundb.net.core.codecs.ObjectEncoder;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.models.auth.Session;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.core.security.auth.SaslFactory;
import com.stundb.net.core.security.auth.callback.handlers.SaslClientCallbackHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

@Slf4j
public class StunDBClientImpl implements StunDBClient {

    private static final String STUNDB = "StunDB";

    static {
        log.info("{} init", StunDBClientImpl.class.getSimpleName());
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    @Inject private Codec codec;

    @Override
    public CompletableFuture<Response> requestAsync(Request request, String ip, Integer port) {
        var future = new CompletableFuture<Response>();
        try {
            var session = authenticate(ip, port).join();
            sessions.put("%s:%s".formatted(ip, port), session);
            execute(ip, port, request.clone(session.sessionId()), future);
            return future;
        } catch (Exception e) {
            log.error("Request failed {}", request, e);
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Response> requestAsync(
            Command command, Object payload, String ip, Integer port) {
        return requestAsync(Request.buildRequest(command, payload), ip, port);
    }

    private void execute(
            String ip, Integer port, Request request, CompletableFuture<Response> promise)
            throws InterruptedException {
        var group = new NioEventLoopGroup(1);
        var channel = connect(ip, port, group, new ClientHandler(promise, request));
        channel.writeAndFlush(request);
        shutdownGracefully(group, channel);
    }

    private CompletableFuture<Session> authenticate(String ip, Integer port) {
        var session = sessions.get("%s:%s".formatted(ip, port));
        if (session != null && !session.isExpired()) {
            var promise = new CompletableFuture<Session>();
            promise.complete(session);
            return promise;
        }

        log.warn("Session is invalid {}", session);
        var subject = new Subject();
        var promise = new CompletableFuture<Session>();

        try {
            new LoginContext(STUNDB, subject).login();
            var saslClient =
                    SaslFactory.createSaslClient(
                            STUNDB,
                            STUNDB,
                            ScramMechanism.SCRAM_SHA_256,
                            null,
                            new SaslClientCallbackHandler(ScramMechanism.SCRAM_SHA_256, subject));

            var group = new NioEventLoopGroup(1);
            var channel = connect(ip, port, group, new SaslClientHandler(saslClient, promise));
            channel.writeAndFlush(
                    new String(saslClient.evaluateChallenge(null), StandardCharsets.UTF_8));
            shutdownGracefully(group, channel);
            return promise;
        } catch (Exception e) {
            log.error("Authentication failed {}", session, e);
            promise.completeExceptionally(e);
            return promise;
        }
    }

    private Channel connect(
            String ip, Integer port, NioEventLoopGroup group, ChannelInboundHandler handler)
            throws InterruptedException {
        var bootstrap = new Bootstrap();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) {
                                var pipeline = channel.pipeline();
                                pipeline.addLast("decoder", new ObjectDecoder(codec));
                                pipeline.addLast("encoder", new ObjectEncoder(codec));
                                ofNullable(handler)
                                        .ifPresent(
                                                h ->
                                                        pipeline.addLast(
                                                                handler.getClass().getSimpleName(),
                                                                h));
                            }
                        });

        return bootstrap.connect(ip, port).sync().channel();
    }

    private void shutdownGracefully(NioEventLoopGroup group, Channel channel) {
        channel.closeFuture()
                .addListener(
                        (ChannelFutureListener)
                                f -> {
                                    if (!f.isSuccess()) {
                                        log.info("Connection closed successfully.");
                                    }
                                    // Gracefully shut down the event loop group after closing the
                                    // connection
                                    group.shutdownGracefully();
                                });
    }
}
