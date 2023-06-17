package com.stundb.timers;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcClient;
import com.stundb.models.UniqueId;
import com.stundb.service.AsyncService;
import com.stundb.service.ListNodesRequest;
import com.stundb.service.ListNodesResponse;
import com.stundb.service.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.TimerTask;

@Singleton
public class CoordinatorTimerTask extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(CoordinatorTimerTask.class);

    @Inject
    private Cache<Node> internalCache;

    @Inject
    @Named("electionService")
    private AsyncService election;

    @Inject
    private GrpcClient client;

    @Inject
    private UniqueId uniqueId;

    @Override
    public void run() {
        internalCache.getAll()
                .stream()
                .filter(Node::getLeader)
                .findFirst()
                .ifPresentOrElse(leader -> retrieveNodes(leader).ifPresent(this::updateInternalCache), election::run);
    }

    private void updateInternalCache(ListNodesResponse response) {
        response.getNodesList()
                .stream()
                .filter(node -> internalCache.get(String.valueOf(node.getUniqueId())).isEmpty())
                .forEach(node -> internalCache.put(String.valueOf(node.getUniqueId()), node));
    }

    private Optional<ListNodesResponse> retrieveNodes(Node node) {
        try {
            if (uniqueId.getNumber() != node.getUniqueId()) {
                return client.run(node.getIp(), node.getPort(), ListNodesRequest.newBuilder().build())
                        .map(r -> (ListNodesResponse) r);
            }
        } catch(Exception e) {
            logger.error("Error contacting node", e);
        }
        return Optional.empty();
    }
}
