package com.stundb.core.models;

import java.util.Map;

public record TcpClient(Integer threads) {

    public TcpClient(Map<String, String> map) {
        this(Integer.parseInt(map.get("threads")));
    }
}
