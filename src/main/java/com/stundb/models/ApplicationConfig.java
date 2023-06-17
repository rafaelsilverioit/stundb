package com.stundb.models;

import com.stundb.models.Capacity;
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
    private String digestAlgorithm;
    private List<String> seeds;
}
