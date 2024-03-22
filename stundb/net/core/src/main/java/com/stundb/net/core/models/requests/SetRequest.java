package com.stundb.net.core.models.requests;

public record SetRequest(String key, Object value, Long ttl) {
}
