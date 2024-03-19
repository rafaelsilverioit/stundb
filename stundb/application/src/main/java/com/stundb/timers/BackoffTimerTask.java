package com.stundb.timers;

import java.util.function.Function;

public interface BackoffTimerTask {

    void enqueue(String seed, Function<String, Void> task);
}
