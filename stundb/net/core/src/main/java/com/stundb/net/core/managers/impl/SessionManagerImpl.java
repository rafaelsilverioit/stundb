package com.stundb.net.core.managers.impl;

import com.stundb.net.core.managers.SessionManager;
import com.stundb.net.core.models.auth.Session;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManagerImpl implements SessionManager {

    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public Session createSession(String uniqueId) {
        var sessionId = UUID.randomUUID().toString();
        var session = new Session(sessionId, uniqueId, LocalDateTime.now().plusSeconds(30));
        sessions.put(sessionId, session);
        return session;
    }

    public void invalidateSession(String sessionId) {
        Optional.ofNullable(sessionId).ifPresent(sessions::remove);
    }

    public boolean isValidSession(String sessionId) {
        return sessionId != null
                && !Optional.ofNullable(sessions.get(sessionId))
                        .map(Session::isExpired)
                        .orElse(true);
    }
}
