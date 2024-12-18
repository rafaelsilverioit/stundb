package com.stundb.service.impl;

import static com.stundb.net.core.models.NodeStatus.State.FAILING;
import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.service.ElectionService;
import com.stundb.utils.NodeUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Singleton
public class ElectionServiceImpl implements ElectionService {

    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicBoolean electionStarted = new AtomicBoolean(false);

    @Inject private Cache<Node> internalCache;
    @Inject private StunDBClient client;
    @Inject private ApplicationConfig config;
    @Inject private UniqueId uniqueId;
    @Inject private NodeUtils utils;

    @Loggable
    @Override
    public void run(Boolean force) {
        var currentLeader = internalCache.get(uniqueId.text()).map(Node::leader).orElse(false);
        if (!force && (currentLeader || electionStarted.get() || counter.getAndIncrement() < 3)) {
            return;
        }

        electionStarted.set(true);
        counter.set(0);
        runElection();
    }

    @Loggable
    @Override
    public void finished() {
        electionStarted.set(false);
    }

    @Loggable
    private void runElection() {
        var nodes = internalCache.getAll();
        var candidates =
                nodes.stream()
                        .filter(
                                node ->
                                        node.uniqueId() > uniqueId.number()
                                                && RUNNING.equals(node.status().state()))
                        .toList();

        // TODO: rethink how we will handle elections
        if (!candidates.isEmpty() && electionStarted.get()) {
            candidates.forEach(this::startElection);
        } else if (electionStarted.get()) {
            becomeLeader(nodes);
        }
    }

    @Loggable
    private void becomeLeader(Collection<Node> nodes) {
        var leader =
                internalCache
                        .get(uniqueId.text())
                        .map(n -> n.clone(true))
                        .orElse(
                                new Node(
                                        config.ip(),
                                        config.port(),
                                        uniqueId.number(),
                                        true,
                                        NodeStatus.create(RUNNING)));

        // notifying other nodes that I became the cluster's leader
        utils.filterNodesByState(nodes, uniqueId.number(), List.of(RUNNING))
                .forEach(n -> notifyAboutElectedLeader(leader, n));

        internalCache.upsert(leader.uniqueId().toString(), leader);

        // setting leader=false for any other node
        utils.filterNodesByState(nodes, uniqueId.number(), List.of(RUNNING))
                .filter(Node::leader)
                .forEach(n -> internalCache.upsert(n.uniqueId().toString(), n.clone(false)));
        electionStarted.set(false);
    }

    @Loggable
    private void notifyAboutElectedLeader(Node leader, Node n) {
        log.info("Telling about election to {}", n.uniqueId());
        client.requestAsync(Command.ELECTED, new ElectedRequest(leader), n.ip(), n.port())
                .handle(
                        (response, error) -> {
                            if (error != null) {
                                log.error("Request failed", error);
                                internalCache.upsert(n.uniqueId().toString(), n.clone(FAILING));
                            }
                            return response;
                        });
    }

    @Loggable
    private void startElection(Node node) {
        log.info("Starting election {}", node.uniqueId());
        client.requestAsync(Command.START_ELECTION, null, node.ip(), node.port())
                .handle(
                        (response, error) -> {
                            if (error != null) {
                                log.error("Request failed", error);
                                internalCache.upsert(
                                        node.uniqueId().toString(), node.clone(FAILING));
                            }
                            return response;
                        });
    }
}
