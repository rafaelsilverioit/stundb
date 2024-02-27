package com.stundb.net.core.models;

import java.time.Instant;

public record NodeStatus(State state, Instant created, Instant lastUpdate) {

    public static NodeStatus create(State state) {
        return new NodeStatus(state, Instant.now(), Instant.now());
    }

    public static NodeStatus create(State state, Instant created) {
        return new NodeStatus(state, created, Instant.now());
    }

    public enum State {
        RUNNING,
        FAILING,
        DISABLED
    }
}
