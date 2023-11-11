package com.stundb.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capacity {
    private Integer publicCache;
    private Integer internalCache;

    public Capacity(Map<String, String> map) {
        this.publicCache = Integer.parseInt(map.get("publicCache"));
        this.internalCache = Integer.parseInt(map.get("internalCache"));
    }
}
