package com.stundb.service;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcClient;
import com.stundb.logging.Loggable;
import com.stundb.models.ApplicationConfig;
import com.stundb.models.UniqueId;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class ElectionService extends AsyncService {

    private final Logger logger = LoggerFactory.getLogger(ElectionService.class);

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private GrpcClient client;

    @Inject
    private ApplicationConfig config;

    @Inject
    private UniqueId uniqueId;

    @Loggable
    @Override
    public void execute() {
        Collection<Node> nodes = internalCache.getAll();
        if (nodes.stream()
                .anyMatch(n -> n.getLeader() && uniqueId.getNumber() == n.getUniqueId())) {
            return;
        }

        var candidates = nodes.stream()
                .filter(node -> node.getUniqueId() > uniqueId.getNumber())
                .toList();

        if (!candidates.isEmpty()) {
            var anySuccessfulResponses = candidates.stream()
                    .map(this::startElection)
                    .reduce((left, right) -> left || right)
                    .orElse(false);

            if (anySuccessfulResponses) {
                logger.info("Got successful responses");
                return;
            }
        }
        becomeLeader(uniqueId.getNumber(), nodes);
    }

    @Loggable
    private void becomeLeader(long uniqueId, Collection<Node> nodes) {
        var node = nodes.stream()
                .filter(n -> uniqueId == n.getUniqueId())
                .findFirst()
                .map(n -> Node.newBuilder(n).setLeader(true).build())
                .orElse(
                    Node.newBuilder()
                            .setIp(config.getIp())
                            .setPort(config.getPort())
                            .setUniqueId(uniqueId)
                            .setLeader(true)
                            .build());

        nodes.stream()
                .filter(n -> n.getUniqueId() != uniqueId)
                .forEach(n -> notifyAboutElectedLeader(node, n));
        internalCache.put(this.uniqueId.getText(), node);
        nodes.stream()
                .filter(n -> n.getLeader() && n.getUniqueId() != this.uniqueId.getNumber())
                .forEach(n -> internalCache.put(String.valueOf(n.getUniqueId()), Node.newBuilder(n).setLeader(false).build()));
    }

    @Loggable
    private void notifyAboutElectedLeader(Node node, Node n) {
        try {
            logger.info("Telling about election to {}", n.getUniqueId());
            client.run(n.getIp(), n.getPort(), ElectedRequest.newBuilder().setLeader(node).build());
        } catch(StatusRuntimeException e) {
            logger.error("Error contacting node", e);
        }
    }

    @Loggable
    private Boolean startElection(Node node) {
        try {
            logger.info("Starting election {}", node.getUniqueId());
            client.run(node.getIp(), node.getPort(), StartElectionRequest.newBuilder().build());
            return true;
        } catch(StatusRuntimeException e) {
            logger.error("Error contacting node", e);
            return false;
        }
    }
}
