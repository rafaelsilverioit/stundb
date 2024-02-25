package com.stundb.net.core.models;

import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Command {
    SET("set", SetRequest.class, Object.class),
    GET("get", GetRequest.class, GetResponse.class),
    DEL("del", DelRequest.class, Object.class),
    EXISTS("exists", ExistsRequest.class, ExistsResponse.class),
    DUMP("dump", Object.class, DumpResponse.class),
    CAPACITY("capacity", Object.class, CapacityResponse.class),
    IS_EMPTY("is_empty", Object.class, IsEmptyResponse.class),
    CLEAR("clear", Object.class, Object.class),
    PING("ping", Object.class, Object.class),
    REGISTER("register", RegisterRequest.class, RegisterResponse.class),
    DEREGISTER("deregister", DeregisterRequest.class, Object.class),
    SYNCHRONIZE("synchronize", CRDTRequest.class, Object.class),
    LIST("list", Object.class, ListNodesResponse.class),
    START_ELECTION("start_election", Object.class, Object.class),
    ELECTED("elected", ElectedRequest.class, Object.class);

    private final String name;
    private final Class<?> requestClass;
    private final Class<?> responseClass;
}
