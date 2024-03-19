package com.stundb.api.models;

public record Executors(
        Executor tcpClient,
        Executor mainServerLoop,
        Executor secondaryServerLoop,
        Executor initializer,
        Executor scheduler) {}
