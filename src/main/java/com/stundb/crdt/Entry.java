package com.stundb.crdt;

import lombok.Builder;

@Builder(toBuilder = true)
public record Entry (long timestamp, String key, String value) {

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
