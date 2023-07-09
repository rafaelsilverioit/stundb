package com.stundb.service;

public interface ElectionService {

    void run(Boolean force);

    default void run() {
        run(false);
    }

    void finished();
}
