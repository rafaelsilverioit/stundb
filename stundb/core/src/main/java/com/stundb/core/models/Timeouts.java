package com.stundb.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Timeouts {

    private Integer tcpReadTimeout;

    private Integer tcpWriteTimeout;

    public Timeouts(Map<String, String> map) {
        this.tcpReadTimeout = Integer.parseInt(map.get("tcpReadTimeout"));
        this.tcpWriteTimeout = Integer.parseInt(map.get("tcpWriteTimeout"));
    }
}
