package com.stundb.net.server.handlers;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;

import io.netty.channel.Channel;

import jakarta.inject.Singleton;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Singleton
@NoArgsConstructor
public class DefaultCommandHandler implements CommandHandler {

    @Override
    public boolean isSupported(Request request) {
        return Arrays.stream(Command.values()).noneMatch(value -> value.equals(request.command()));
    }

    @Override
    public void execute(Request request, Channel channel) {
        log.error(
                """
                        No command handler found for: {} \n
                        If a new command handler has been introduced, make sure to update the SPI file.
                """,
                request.command());
        var response = new ErrorResponse("error.invalid.command");
        writeAndFlush(request, response, Status.ERROR, channel);
    }
}
