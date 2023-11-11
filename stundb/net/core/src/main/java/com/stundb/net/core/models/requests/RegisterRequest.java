package com.stundb.net.core.models.requests;

public record RegisterRequest(String ip, Integer port, Long uniqueId) {
}
