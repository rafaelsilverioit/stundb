package com.stundb.net.server.handlers;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;

import io.netty.channel.Channel;

import jakarta.inject.Singleton;

import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Singleton
@NoArgsConstructor
public class DefaultCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean isSupported(Request request) {
        return Arrays.stream(Command.values()).noneMatch(value -> value.equals(request.command()));
    }

    @Override
    public void execute(Request request, Channel channel) {
        logger.error(
                "No command handler found for: {} - If a new command handler has been introduced, "
                        + "make sure to update the SPI file.",
                request.command());
        var result = new ErrorResponse("error.invalid.command");
        channel.writeAndFlush(result);
        writeAndFlush(request, result, Status.ERROR, channel);
    }
}
