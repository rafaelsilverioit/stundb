package com.stundb.net.core.models;

import com.stundb.net.core.models.NodeStatus.State;

public record Node(String ip, Integer port, Long uniqueId, Boolean leader, NodeStatus status) {

    public Node clone(Boolean leader) {
        return new Node(ip, port, uniqueId, leader, status);
    }

    public Node clone(State state) {
        return new Node(ip, port, uniqueId, leader, NodeStatus.create(state, status.created()));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var other = (Node) object;
        return ip.equals(other.ip())
                && port.equals(other.port())
                && uniqueId.equals(other.uniqueId);
    }
}
