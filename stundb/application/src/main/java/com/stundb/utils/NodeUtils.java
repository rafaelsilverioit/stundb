package com.stundb.utils;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;

import jakarta.inject.Inject;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Stream;

public class NodeUtils {

    @Inject private MessageDigest digester;

    public long generateUniqueId(String key) {
        digester.reset();
        digester.update(key.getBytes());

        byte[] digest = digester.digest();

        long hash = 0;
        for (int i = 0; i < 4; i++) {
            hash <<= 8;
            hash |= ((int) digest[i]) & 0xFF;
        }

        return hash;
    }

    public Stream<Node> filterNodesByState(
            Collection<Node> nodes, Long uniqueId, List<NodeStatus.State> states) {
        return nodes.stream()
                .filter(
                        node ->
                                !Objects.equals(node.uniqueId(), uniqueId)
                                        && states.contains(node.status().state()));
    }
}
