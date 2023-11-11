package com.stundb.net.core.models.responses;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;
import com.stundb.net.core.models.requests.Request;

public record Response(Version version, Status status, Command command, Type type, Object payload) {

    public static Response buildResponse(Request request, Status status, Object payload) {
        return new Response(request.version(), status, request.command(), request.type(), payload);
    }
}
