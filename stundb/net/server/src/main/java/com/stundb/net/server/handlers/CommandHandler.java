package com.stundb.net.server.handlers;

import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import io.netty.channel.Channel;

public interface CommandHandler {

    default boolean isSupported(Request request) {
        return false;
    }

    void execute(Request request, Channel channel);

    default void writeAndFlush(Request request, Object data, Channel channel) {
        writeAndFlush(request, data, Status.OK, channel);
    }

    default void writeAndFlush(Request request, Object data, Status status, Channel channel) {
        var response = Response.buildResponse(request, status, data);
        channel.writeAndFlush(response);
    }
}
