package com.stundb.api.models;

import java.util.Map;

public record Capacity(Integer publicCache, Integer internalCache) {

    public Capacity(Map<String, String> map) {
        this(
                Integer.parseInt(map.getOrDefault("publicCache", "100")),
                Integer.parseInt(map.getOrDefault("internalCache", "10")));
    }
}
