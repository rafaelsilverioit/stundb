package com.stundb.net.core.models.requests;

public record SetRequest(String key, byte[] value, Long ttl) {}
