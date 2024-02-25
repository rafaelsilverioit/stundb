package com.stundb.core.models;

import java.time.Instant;

public record Status(State state, Instant created, Instant lastUpdate) {

    public static Status create(State state) {
        return new Status(state, Instant.now(), Instant.now());
    }

    public static Status create(State state, Instant created) {
        return new Status(state, created, Instant.now());
    }

    public enum State {
        RUNNING,
        FAILING,
        DISABLED
    }
}
