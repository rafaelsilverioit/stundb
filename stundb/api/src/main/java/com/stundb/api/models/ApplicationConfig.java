package com.stundb.api.models;


import java.util.List;
import java.util.Map;

public record ApplicationConfig(
        String name,
        String ip,
        Integer port,
        Boolean statePersistenceEnabled,
        String stateDir,
        Capacity capacity,
        Timeouts timeouts,
        Executors executors,
        String digestAlgorithm,
        List<String> seeds,
        Map<String, Integer> backoffSettings) {}
