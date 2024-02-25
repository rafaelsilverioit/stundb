package com.stundb.core.crdt;

import java.time.Instant;

public record Entry(Instant timestamp, String key, Object value) {

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var other = (Entry) object;
        return key.equals(other.key()) && timestamp.equals(other.timestamp());
    }

    public Entry cloneRemovingValue() {
        return new Entry(timestamp, key, null);
    }
}
