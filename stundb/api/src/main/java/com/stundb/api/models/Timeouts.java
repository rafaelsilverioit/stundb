package com.stundb.api.models;

import java.util.Map;

public record Timeouts(Integer tcpReadTimeout, Integer tcpWriteTimeout) {

    public Timeouts(Map<String, String> map) {
        this(
                Integer.parseInt(map.getOrDefault("tcpReadTimeout", "10")),
                Integer.parseInt(map.getOrDefault("tcpWriteTimeout", "10")));
    }
}
