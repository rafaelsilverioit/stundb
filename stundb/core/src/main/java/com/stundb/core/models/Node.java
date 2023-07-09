package com.stundb.core.models;

public record Node(String ip, Integer port, Long uniqueId, Boolean leader, Status status) {

    public Node clone(Boolean leader) {
        return new Node(ip, port, uniqueId, leader, status);
    }

    public Node clone(Status.State state) {
        return new Node(ip, port, uniqueId, leader, Status.create(state, status.created()));
    }
}
