package com.stundb.net.core.managers.impl;

import com.stundb.net.core.managers.RequestManager;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RequestManagerImpl implements RequestManager {

    private final int capacity = 32;
    private final Deque<String> deque;

    RequestManagerImpl() {
        this.deque = new ArrayDeque<>(capacity);
    }

    @Override
    public void offer(String element) {
        if (deque.size() == capacity) {
            deque.pollFirst();
        }
        deque.offerLast(element);
    }

    @Override
    public boolean contains(String element) {
        return deque.contains(element);
    }
}
