package com.stundb.observers;

import com.stundb.cache.Cache;
import com.stundb.service.Node;
import com.stundb.service.RegisterResponse;
import com.stundb.service.ReplicationService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class RegisterResponseObserver implements StreamObserver<RegisterResponse> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String seed;

    private ReplicationService replicationService;

    private Cache<Node> internalCache;

    @Override
    public void onNext(RegisterResponse data) {
        var nodes = data.getNodesList();
        logger.info("Reply from seed {} - {} nodes", seed, nodes.size());
        nodes.forEach(node -> internalCache.put(String.valueOf(node.getUniqueId()), node));
        replicationService.synchronize(data.getState());
    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    @Override
    public void onCompleted() {

    }
}
