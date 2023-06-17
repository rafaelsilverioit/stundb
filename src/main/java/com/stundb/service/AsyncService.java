package com.stundb.service;

import java.util.concurrent.CompletableFuture;

public abstract class AsyncService {

    protected abstract void execute();

    public CompletableFuture<Void> run() {
        return CompletableFuture.runAsync(this::execute);
    }
}
