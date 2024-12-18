package com.stundb.net.client.handlers;

import com.stundb.net.core.models.auth.Session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

@Slf4j
@AllArgsConstructor
public class SaslClientHandler extends SimpleChannelInboundHandler<String> {

    private final SaslClient saslClient;
    private final CompletableFuture<Session> promise;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws SaslException {
        var response = saslClient.evaluateChallenge(msg.getBytes(StandardCharsets.UTF_8));
        if (saslClient.isComplete()) {
            // TODO: handle in a neater way/similar to ScramFormatter
            var extensions =
                    Arrays.stream(msg.split(","))
                            .skip(1)
                            .map(data -> data.split("="))
                            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

            var session = extensions.get("session");
            var username = extensions.get("username");
            var expiration = extensions.get("expiration");
            var expirationDatetime = LocalDateTime.parse(expiration);

            promise.complete(new Session(session, username, expirationDatetime));
            log.info("SCRAM handshake completed - {}", session);
            ctx.close();
            return;
        }
        ctx.writeAndFlush(new String(response, StandardCharsets.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        promise.completeExceptionally(cause);
    }
}
