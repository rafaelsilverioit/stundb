package com.stundb.timers;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcRunner;
import com.stundb.models.UniqueId;
import com.stundb.observers.ListNodesResponseObserver;
import com.stundb.service.AsyncService;
import com.stundb.service.ListNodesRequest;
import com.stundb.service.ListNodesResponse;
import com.stundb.service.Node;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.TimerTask;

@Singleton
public class CoordinatorTimerTask extends TimerTask {

    @Inject
    private Cache<Node> internalCache;

    @Inject
    @Named("electionService")
    private AsyncService election;

    @Inject
    private GrpcRunner<ListNodesRequest, ListNodesResponse> client;

    @Inject
    private UniqueId uniqueId;

    @Override
    public void run() {
        internalCache.getAll()
                .stream()
                .filter(node -> node.getLeader() && node.getUniqueId() != uniqueId.getNumber())
                .findFirst()
                .ifPresentOrElse(this::retrieveNodes, election::run);
    }

    private void retrieveNodes(Node node) {
        client.run(
                node.getIp(),
                node.getPort(),
                ListNodesRequest.newBuilder().build(),
                new ListNodesResponseObserver(internalCache));
    }
}
