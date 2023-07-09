package com.stundb.net.server;

import com.stundb.net.client.StunDBClient;
import com.stundb.core.cache.Cache;
import com.stundb.core.codecs.Codec;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.models.Node;
import com.stundb.core.models.UniqueId;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.RegisterResponse;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.server.codecs.KryoObjectDecoder;
import com.stundb.net.server.codecs.KryoObjectEncoder;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.net.server.handlers.DefaultCommandHandler;
import com.stundb.net.server.handlers.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.*;

import static com.stundb.core.models.Status.State.RUNNING;
import static java.lang.String.format;

@NoArgsConstructor
public abstract class TcpServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NioEventLoopGroup mainGroup = new NioEventLoopGroup(10);

    private final NioEventLoopGroup secondaryGroup = new NioEventLoopGroup(10);

    @Inject
    protected StunDBClient client;

    @Inject
    private ApplicationConfig config;

    @Inject
    private Codec codec;

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private List<? extends CommandHandler> runners;

    @Inject
    private DefaultCommandHandler defaultCommandHandler;

    @Inject
    private UniqueId uniqueId;

    protected abstract void onStart();

    protected abstract void synchronize(CRDTRequest data);

    public void run() {
        var executor = Executors.newFixedThreadPool(10);
        var bootstrap = new ServerBootstrap();
        bootstrap.group(mainGroup, secondaryGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer())
                .bind(new InetSocketAddress(config.getPort()))
                .addListener(onStartListener(executor));
    }

    @Loggable
    private ChannelInitializer<Channel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Loggable
            @Override
            protected void initChannel(Channel channel) {
                var pipeline = channel.pipeline();
                var timeouts = config.getTimeouts();
                var tcpReadTimeout = timeouts.getTcpReadTimeout();
                var tcpWriteTimeout = timeouts.getTcpWriteTimeout();

                pipeline.addFirst(new LoggingHandler(LogLevel.INFO));
                pipeline.addFirst(new ReadTimeoutHandler(tcpReadTimeout, TimeUnit.SECONDS));
                pipeline.addFirst(new WriteTimeoutHandler(tcpWriteTimeout, TimeUnit.SECONDS));
                pipeline.addFirst("idleStateHandler", new IdleStateHandler(tcpReadTimeout, tcpWriteTimeout, tcpReadTimeout, TimeUnit.SECONDS));
                pipeline.addLast("decoder", new KryoObjectDecoder(codec));
                pipeline.addLast("encoder", new KryoObjectEncoder(codec));
                pipeline.addLast("handler", new RequestHandler(runners, defaultCommandHandler));
                logger.debug("----> [" + channel.id() + "] connected!");
            }
        };
    }

    private ChannelFutureListener onStartListener(ExecutorService executor) {
        return channelFuture -> {
            initialize(executor)
                    .handle((response, err) -> {
                        if (!channelFuture.isSuccess()) {
                            logger.error("Server failed to start", channelFuture.cause());
                            System.exit(1);
                        } else if (err != null) {
                            logger.error("Server failed to start", err);
                            System.exit(1);
                        } else if (internalCache.isEmpty() && !config.getSeeds().contains(config.getIp() + ":" + config.getPort())) {
                            logger.error("Node list is empty");
                            System.exit(1);
                        } else if (internalCache.isEmpty() && config.getSeeds().contains(config.getIp() + ":" + config.getPort())) {
                            var myself = new Node(
                                    config.getIp(),
                                    config.getPort(),
                                    uniqueId.getNumber(),
                                    false,
                                    com.stundb.core.models.Status.create(RUNNING));
                            internalCache.put(uniqueId.getText(), myself);
                        }

                        onStart();
                        logger.info("Running {} on {}:{}", config.getName(), config.getIp(), config.getPort());
                        return response;
                    })
                    .get();
            executor.shutdown();
        };
    }

    private CompletableFuture<Void> initialize(ExecutorService executor) throws RuntimeException {
        return CompletableFuture.runAsync(() -> {
            List<String> seeds = config.getSeeds();
            if (seeds.isEmpty()) {
                throw new IllegalArgumentException("A list of seeds must be provided");
            }

            seeds.stream()
                    .filter(seed -> !seed.equals(config.getIp() + ":" + config.getPort()))
                    .forEach(seed -> {
                            try {
                                var response = contactSeed(seed);
                                if (Status.ERROR.equals(response.status())) {
                                    var code = ((ErrorResponse) response.payload()).code();
                                    throw new RuntimeException(format("Reply from seed was - %s", code));
                                }
                                var data = (RegisterResponse) response.payload();
                                data.nodes().forEach(entry -> internalCache.put(entry.uniqueId().toString(), entry));
                                // TODO: perhaps we should change how synchronization works
                                synchronize(data.state());
                            } catch (Exception e) {
                                logger.error("Failed to contact seed " + seed, e);
                            }
                    });
        }, executor);
    }

    private Response contactSeed(String seed) throws ExecutionException, InterruptedException {
        var address = seed.split(":");
        if (address.length != 2) {
            throw new IllegalArgumentException("Invalid seed address " + seed);
        }

        return client.requestAsync(getRegisterRequest(), address[0], Integer.parseInt(address[1])).get();
    }

    private Request getRegisterRequest() {
        return Request.buildRequest(
                Command.REGISTER,
                new RegisterRequest(config.getIp(), config.getPort(), uniqueId.getNumber()));
    }
}
