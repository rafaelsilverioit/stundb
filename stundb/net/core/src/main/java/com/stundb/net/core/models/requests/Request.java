package com.stundb.net.core.models.requests;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;

public record Request(Version version, Command command, Type type, Object payload) {

    public static Request buildRequest(Command command, Object payload) {
        return new Request(Version.STUNDB_v1_0, command, Type.RAW, payload);
    }
}
