package com.stundb.acceptance.tests.state;

import com.stundb.net.core.models.Node;

import lombok.Data;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class NodeState {

    private Long nodeId;
    private Collection<Node> nodes;

    public static Node getLeader(Collection<Node> nodes) {
        return nodes.stream()
                .filter(Node::leader)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Cluster has not elected a leader yet"));
    }

    public Optional<Node> getLeader() {
        return Optional.ofNullable(nodes).map(NodeState::getLeader);
    }

    public boolean isPresent(Long nodeId) {
        return Stream.ofNullable(nodes)
                .flatMap(Collection::stream)
                .anyMatch(n -> nodeId.equals(n.uniqueId()));
    }
}
