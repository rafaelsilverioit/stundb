package com.stundb.core.models;

import java.util.Map;

public record Timeouts(Integer tcpReadTimeout, Integer tcpWriteTimeout) {

    public Timeouts(Map<String, String> map) {
        this(
                Integer.parseInt(map.get("tcpReadTimeout")),
                Integer.parseInt(map.get("tcpWriteTimeout")));
    }
}
