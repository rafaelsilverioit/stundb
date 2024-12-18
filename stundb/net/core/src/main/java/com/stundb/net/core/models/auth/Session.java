package com.stundb.net.core.models.auth;

import java.time.LocalDateTime;

public record Session(String sessionId, String uniqueId, LocalDateTime expiration) {

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiration);
    }
}
