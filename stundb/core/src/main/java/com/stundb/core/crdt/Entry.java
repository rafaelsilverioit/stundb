package com.stundb.core.crdt;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record Entry (Instant timestamp, String key, Object value) {

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var other = (Entry) object;
        return key.equals(other.key()) && timestamp == other.timestamp();
    }
}
