package com.stundb.net.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum Version {
    STUNDB_v1_0("StunDB 1.0");

    private final String value;
}
