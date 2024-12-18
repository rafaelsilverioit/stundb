package com.stundb.net.server.handlers;

import com.stundb.net.core.managers.SessionManager;
import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.security.auth.SaslFactory;
import com.stundb.net.core.security.auth.callback.handlers.ScramServerCallbackHandler;
import com.stundb.net.core.security.auth.credentials.CredentialManager;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import javax.security.sasl.SaslServer;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class SaslServerHandler extends SimpleChannelInboundHandler<String> {

    private final CredentialManager credentialManager;
    private final SessionManager sessionManager;
    private SaslServer saslServer;
    private ScramServerCallbackHandler callbackHandler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        callbackHandler = new ScramServerCallbackHandler(credentialManager);
        saslServer =
                SaslFactory.createSaslServer(
                        ScramMechanism.SCRAM_SHA_256, "StunDB", null, callbackHandler);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        var message =
                new String(
                        saslServer.evaluateResponse(msg.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8);

        if (saslServer.isComplete()) {
            var session = sessionManager.createSession(callbackHandler.getUsername());
            message =
                    message
                            + ",session="
                            + session.sessionId()
                            + ",expiration="
                            + session.expiration().toString()
                            + ",username="
                            + session.uniqueId();
        }
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        var message =
                switch (cause) {
                    case ReadTimeoutException __ -> null;
                    case IllegalStateException __ -> "other-error";
                    default -> cause.getMessage();
                };
        if (message == null) {
            return;
        }
        log.warn(message, cause);
        ctx.writeAndFlush("e=" + message);
    }
}
