package com.stundb.service.impl;

import static com.stundb.net.core.models.NodeStatus.State.FAILING;
import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.ListNodesResponse;
import com.stundb.net.core.models.responses.RegisterResponse;
import com.stundb.service.ElectionService;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class NodeServiceImpl implements NodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<Long, AtomicInteger> failures = new ConcurrentHashMap<>();

    @Inject private Timer timer;
    @Inject private StunDBClient client;
    @Inject private Cache<Node> internalCache;
    @Inject private ReplicationService replicationService;
    @Inject private ElectionService election;
    @Inject private UniqueId uniqueId;
    @Inject private NodeUtils utils;

    @Inject
    @Named("coordinatorTimerTask")
    private TimerTask coordinatorTimerTask;

    @Loggable
    @Override
    public void init() {
        timer.scheduleAtFixedRate(coordinatorTimerTask, 10, 15 * 1000);
    }

    @Loggable
    @Override
    public void ping(Request request) {}

    @Loggable
    @Override
    public void trackNodeFailure(Node node) {
        if (NodeStatus.State.DISABLED.equals(node.status().state())) {
            return;
        }

        failures.computeIfAbsent(node.uniqueId(), key -> new AtomicInteger(0));
        if (failures.get(node.uniqueId()).incrementAndGet() > 3) {
            internalCache.put(node.uniqueId().toString(), node.clone(FAILING));
            failures.remove(node.uniqueId());
        }
    }

    @Loggable
    @Override
    public RegisterResponse register(Request request) {
        var data = (RegisterRequest) request.payload();
        internalCache.put(
                data.uniqueId().toString(),
                new Node(
                        data.ip(),
                        data.port(),
                        data.uniqueId(),
                        false,
                        NodeStatus.create(RUNNING)));

        utils.filterNodesByState(internalCache.getAll(), uniqueId.number(), List.of(RUNNING))
                .filter(Node::leader)
                .findFirst()
                .ifPresent(
                        node ->
                                client.requestAsync(request, node.ip(), node.port())
                                        .handle(
                                                (response, error) -> {
                                                    if (error != null) {
                                                        logger.error(
                                                                "Leader failed, starting election",
                                                                error);
                                                        internalCache.put(
                                                                node.uniqueId().toString(),
                                                                node.clone(FAILING));
                                                        election.run();
                                                        return error;
                                                    }
                                                    return response;
                                                }));

        return new RegisterResponse(
                internalCache.getAll().stream().toList(), replicationService.generateCrdtRequest());
    }

    @Loggable
    @Override
    public void deregister(DeregisterRequest request) {
        internalCache.del(request.uniqueId().toString());
    }

    @Loggable
    @Override
    public ListNodesResponse list() {
        return new ListNodesResponse(internalCache.getAll());
    }

    @Loggable
    @Override
    public void startElection(Request request) {
        election.run(true);
    }

    @Loggable
    @Override
    public void elected(ElectedRequest request) {
        var node = request.leader();
        logger.info("{} became the leader", node.uniqueId());
        utils.filterNodesByState(internalCache.getAll(), node.uniqueId(), List.of(RUNNING))
                .filter(Node::leader)
                .forEach(n -> internalCache.put(n.uniqueId().toString(), n.clone(false)));
        internalCache.put(node.uniqueId().toString(), node);
        election.finished();
    }

    @Loggable
    @Override
    public void synchronize(CRDTRequest request) {
        replicationService.synchronize(request);
    }
}
