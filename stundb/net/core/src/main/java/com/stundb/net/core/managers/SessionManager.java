package com.stundb.net.core.managers;

import com.stundb.net.core.managers.impl.SessionManagerImpl;
import com.stundb.net.core.models.auth.Session;

public sealed interface SessionManager permits SessionManagerImpl {

    Session createSession(String uniqueId);

    void invalidateSession(String sessionId);

    boolean isValidSession(String sessionId);
}
