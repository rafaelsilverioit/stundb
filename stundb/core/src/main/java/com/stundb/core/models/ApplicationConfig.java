package com.stundb.core.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApplicationConfig {

    private String name;
    private String ip;
    private Integer port;
    private Capacity capacity;
    private Timeouts timeouts;
    private TcpClient tcpClient;
    private String digestAlgorithm;
    private List<String> seeds;
}
