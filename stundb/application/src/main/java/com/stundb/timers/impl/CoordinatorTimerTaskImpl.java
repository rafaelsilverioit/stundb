package com.stundb.timers.impl;

import static com.stundb.net.core.models.NodeStatus.State.DISABLED;
import static com.stundb.net.core.models.NodeStatus.State.FAILING;
import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import static java.util.function.Predicate.not;

import com.stundb.core.cache.Cache;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.DeregisterRequest;
import com.stundb.net.core.models.requests.PingRequest;
import com.stundb.net.core.models.responses.PingResponse;
import com.stundb.service.ElectionService;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class CoordinatorTimerTaskImpl extends TimerTask {

    private static final List<NodeStatus.State> VALID_STATES = List.of(RUNNING);
    private static final List<NodeStatus.State> INVALID_STATES =
            Arrays.stream(NodeStatus.State.values())
                    .filter(not(RUNNING::equals))
                    .collect(Collectors.toList());

    @Inject private Cache<Node> internalCache;
    @Inject private ElectionService election;
    @Inject private StunDBClient client;
    @Inject private ReplicationService replicationService;
    @Inject private UniqueId uniqueId;
    @Inject private NodeUtils utils;

    @Override
    public void run() {
        var currentLeader = internalCache.get(uniqueId.text()).map(Node::leader).orElse(false);
        if (currentLeader) {
            reachFailedNodes();
            return;
        }
        pingLeader();
    }

    private void reachFailedNodes() {
        var nodes = internalCache.getAll();
        utils.filterNodesByState(nodes, uniqueId.number(), INVALID_STATES)
                .forEach(
                        node -> {
                            if (FAILING.equals(node.status().state())) {
                                reachFailedNode(node);
                                return;
                            }

                            internalCache.del(node.uniqueId().toString());
                            notifyAboutLeavingMember(node, nodes);
                        });
    }

    private void notifyAboutLeavingMember(Node node, Collection<Node> nodes) {
        utils.filterNodesByState(nodes, uniqueId.number(), VALID_STATES)
                .filter(n -> !Objects.equals(n.uniqueId(), node.uniqueId()))
                .forEach(
                        n ->
                                client.requestAsync(
                                                Command.DEREGISTER,
                                                new DeregisterRequest(node.uniqueId()),
                                                n.ip(),
                                                n.port())
                                        .handle(
                                                (response, error) -> {
                                                    if (error != null) {
                                                        updateCache(n, FAILING);
                                                        return error;
                                                    }
                                                    return response;
                                                }));
    }

    private void updateCache(Node n, NodeStatus.State state) {
        internalCache.upsert(n.uniqueId().toString(), n.clone(state));
    }

    private void reachFailedNode(Node node) {
        client.requestAsync(
                        Command.PING,
                        new PingRequest(replicationService.generateVersionClock()),
                        node.ip(),
                        node.port())
                .handle(
                        (response, error) -> {
                            if (error != null) {
                                updateCache(node, DISABLED);
                                return error;
                            }
                            updateCache(node, RUNNING);
                            return response;
                        });
    }

    private void pingLeader() {
        utils.filterNodesByState(internalCache.getAll(), uniqueId.number(), VALID_STATES)
                .filter(Node::leader)
                .findFirst()
                .ifPresentOrElse(this::pingLeader, election::run);
    }

    /**
     * Upon a ping response, synchronizes cache state with the leader's state and updates the
     * internal nodes list.
     *
     * @param node the leader node
     */
    private void pingLeader(Node node) {
        client.requestAsync(
                        Command.PING,
                        new PingRequest(replicationService.generateVersionClock()),
                        node.ip(),
                        node.port())
                .handle(
                        (response, throwable) -> {
                            if (throwable != null) {
                                log.error("Error pinging leader", throwable);
                                updateCache(node, FAILING);
                                return throwable;
                            }

                            var data = (PingResponse) response.payload();
                            replicationService.synchronize(data.added(), data.removed());
                            data.nodes()
                                    .forEach(n -> internalCache.upsert(n.uniqueId().toString(), n));
                            return response;
                        });
    }
}
