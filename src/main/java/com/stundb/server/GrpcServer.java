package com.stundb.server;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcRunner;
import com.stundb.models.ApplicationConfig;
import com.stundb.models.UniqueId;
import com.stundb.observers.RegisterResponseObserver;
import com.stundb.service.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@NoArgsConstructor
public class GrpcServer {

    private final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    @Inject
    private StoreServiceGrpc.StoreServiceImplBase storeService;

    @Inject
    private NodesServiceGrpc.NodesServiceImplBase nodeService;

    @Inject
    private ApplicationConfig config;

    @Inject
    private GrpcRunner<RegisterRequest, RegisterResponse> runner;

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private ReplicationService replicationService;

    @Inject
    private UniqueId uniqueId;

    public void run() throws IOException {
        initialize();

        var port = config.getPort();
        var server = ServerBuilder.forPort(port)
                .addService(storeService)
                .addService(nodeService)
                .build();

        server.start();
        logger.info("Running {} on port {}", config.getName(), port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownHooks(server)));
    }

    private void initialize() throws RuntimeException {
        List<String> seeds = config.getSeeds();
        String address = config.getIp() + ":" + config.getPort();

        if (seeds.isEmpty()) {
            throw new IllegalArgumentException("A list of seeds must be provided");
        }

        seeds.forEach(this::contactSeed);

        if (internalCache.isEmpty() && !seeds.contains(address)) {
            logger.error("Nodes list is empty");
            System.exit(1);
        }

        ((NodesServiceImpl) nodeService).init();
    }

    private void contactSeed(String seed) {
        var s = seed.split(":");
        if (s.length != 2) {
            throw new IllegalArgumentException("Invalid seed address " + seed);
        }

        if (seed.equals(config.getIp() + ":" + config.getPort())) {
            return;
        }

        runner.run(
                s[0],
                Integer.parseInt(s[1]),
                getRegisterRequest(),
                new RegisterResponseObserver(seed, replicationService, internalCache));
    }

    private RegisterRequest getRegisterRequest() {
        return RegisterRequest.newBuilder()
                .setIp(config.getIp())
                .setPort(config.getPort())
                .setUniqueId(uniqueId.getNumber())
                .build();
    }

    @SneakyThrows
    private void shutdownHooks(Server server) {
        server.shutdown();
        server.awaitTermination();
    }
}
