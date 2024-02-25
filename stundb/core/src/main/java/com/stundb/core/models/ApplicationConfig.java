package com.stundb.core.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// TODO: make this a record class
@Data
@NoArgsConstructor
public class ApplicationConfig {

    private String name;
    private String ip;
    private Integer port;
    private Capacity capacity;
    private Timeouts timeouts;
    private Executors executors;
    private String digestAlgorithm;
    private List<String> seeds;
    private Map<String, Object> backoffSettings;
}
