package com.stundb.net.core.models.requests;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;

import java.util.UUID;

public record Request(
        Version version,
        String session,
        String requestId,
        Command command,
        Type type,
        Object payload) {

    public static Request buildRequest(Command command, Object payload) {
        return new Request(
                Version.STUNDB_v1_0,
                null,
                UUID.randomUUID().toString(),
                command,
                Type.RAW,
                payload);
    }

    public Request clone(String session) {
        return new Request(version, session, requestId, command, type, payload);
    }
}
