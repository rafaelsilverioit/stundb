package com.stundb.server;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcClient;
import com.stundb.models.ApplicationConfig;
import com.stundb.models.UniqueId;
import com.stundb.service.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
public class GrpcServer {

    private final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    @Inject
    private KVStoreServiceGrpc.KVStoreServiceImplBase storeService;

    @Inject
    private NodesServiceGrpc.NodesServiceImplBase nodeService;

    @Inject
    private ApplicationConfig config;

    @Inject
    private GrpcClient client;

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

        try {
            var data = client.run(
                    s[0],
                    Integer.parseInt(s[1]),
                    RegisterRequest.newBuilder()
                            .setIp(config.getIp())
                            .setPort(config.getPort())
                            .setUniqueId(uniqueId.getNumber())
                            .build())
                    .map(response -> (RegisterResponse) response);
            var nodes = data
                    .map(RegisterResponse::getNodesList)
                    .stream()
                    .flatMap(Collection::stream)
                    .toList();

            logger.info("Reply from seed {} - {} nodes", seed, nodes.size());
            nodes.forEach(node -> internalCache.put(String.valueOf(node.getUniqueId()), node));
            data.ifPresent(response -> replicationService.synchronize(response.getState()));
        } catch (Exception e) {
            logger.error("Failed to contact seed", e);
        }
    }

    @SneakyThrows
    private void shutdownHooks(Server server) {
        server.shutdown();
        server.awaitTermination();
    }
}
