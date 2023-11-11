package com.stundb.timers;

import com.stundb.core.cache.Cache;
import com.stundb.core.models.Node;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.DeregisterRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.ListNodesResponse;
import com.stundb.service.ElectionService;
import com.stundb.utils.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;

import static com.stundb.core.models.Status.State.*;

@Singleton
public class CoordinatorTimerTask extends TimerTask {

    private static final List<com.stundb.core.models.Status.State> VALID_STATES = List.of(RUNNING);

    private static final List<com.stundb.core.models.Status.State> INVALID_STATES = List.of(FAILING, DISABLED);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private ElectionService election;

    @Inject
    private StunDBClient client;

    @Inject
    private UniqueId uniqueId;

    @Inject
    private NodeUtils utils;

    @Override
    public void run() {
        var currentLeader = internalCache.get(uniqueId.getText()).map(Node::leader).orElse(false);
        if (currentLeader) {
            reachFailedNodes();
            return;
        }
        retrieveNodesFromLeader();
    }

    private void reachFailedNodes() {
        var nodes = internalCache.getAll();
        utils.filterNodesByState(nodes, uniqueId.getNumber(), INVALID_STATES)
                .forEach(node -> {
                    if (FAILING.equals(node.status().state())) {
                        reachFailedNode(node);
                        return;
                    }

                    internalCache.del(node.uniqueId().toString());
                    notifyAboutLeavingMember(node, nodes);
                });
    }

    private void notifyAboutLeavingMember(Node node, Collection<Node> nodes) {
        var request = Request.buildRequest(Command.DEREGISTER, new DeregisterRequest(node.uniqueId()));
        utils.filterNodesByState(nodes, uniqueId.getNumber(), VALID_STATES)
                .filter(n -> !Objects.equals(n.uniqueId(), node.uniqueId()))
                .forEach(n ->
                        client.requestAsync(request, n.ip(), n.port())
                                .handle((response, error) -> {
                                    if (error != null) {
                                        updateCache(n, FAILING);
                                        return error;
                                    }
                                    return response;
                                }));
    }

    private void updateCache(Node n, com.stundb.core.models.Status.State state) {
        internalCache.put(n.uniqueId().toString(), n.clone(state));
    }

    private void reachFailedNode(Node node) {
        client.requestAsync(Request.buildRequest(Command.PING, null), node.ip(), node.port())
                .handle((response, error) -> {
                    if (error != null) {
                        updateCache(node, DISABLED);
                        return error;
                    }
                    updateCache(node, RUNNING);
                    return response;
                });
    }

    private void retrieveNodesFromLeader() {
        utils.filterNodesByState(internalCache.getAll(), uniqueId.getNumber(), VALID_STATES)
                .filter(Node::leader)
                .findFirst()
                .ifPresentOrElse(this::retrieveNodes, election::run);
    }

    private void retrieveNodes(Node node) {
        client.requestAsync(Request.buildRequest(Command.LIST, null), node.ip(), node.port())
                .handle((response, throwable) -> {
                    if (throwable != null) {
                        logger.error("Error retrieving nodes", throwable);
                        updateCache(node, FAILING);
                        return throwable;
                    } else if (Status.ERROR.equals(response.status())) {
                        logger.error("Response was " + ((ErrorResponse) response.payload()).code());
                        return response;
                    }

                    ((ListNodesResponse) response.payload()).nodes()
                            .forEach(n -> internalCache.put(n.uniqueId().toString(), n));
                    return response;
                });
    }
}
