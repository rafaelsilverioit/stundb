package com.stundb.observers;

import com.stundb.cache.Cache;
import com.stundb.service.ListNodesResponse;
import com.stundb.service.Node;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class ListNodesResponseObserver implements StreamObserver<ListNodesResponse> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Cache<Node> internalCache;

    @Override
    public void onNext(ListNodesResponse response) {
        response.getNodesList()
                .stream()
                .filter(node -> internalCache.get(String.valueOf(node.getUniqueId())).isEmpty())
                .forEach(node -> internalCache.put(String.valueOf(node.getUniqueId()), node));
    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    @Override
    public void onCompleted() {

    }
}
