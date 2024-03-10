package com.stundb.net.core.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Command {
    SET,
    GET,
    DEL,
    EXISTS,
    DUMP,
    CAPACITY,
    IS_EMPTY,
    CLEAR,
    PING,
    REGISTER,
    DEREGISTER,
    SYNCHRONIZE,
    LIST,
    START_ELECTION,
    ELECTED
}
