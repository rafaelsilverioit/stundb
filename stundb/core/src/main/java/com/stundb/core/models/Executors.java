package com.stundb.core.models;

public record Executors(
        Executor tcpClient,
        Executor mainServerLoop,
        Executor secondaryServerLoop,
        Executor initializer) {}
