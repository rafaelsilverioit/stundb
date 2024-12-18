package com.stundb.net.server.handlers;

import com.stundb.core.logging.Loggable;
import com.stundb.net.core.managers.RequestManager;
import com.stundb.net.core.managers.SessionManager;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.Response;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class RequestHandler extends SimpleChannelInboundHandler<Request> {

    private final List<? extends CommandHandler> handlers;
    private final DefaultCommandHandler defaultCommandHandler;
    private final SessionManager sessionManager;
    private final RequestManager requestManager;

    @Loggable
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) {
        if (requestManager.contains(request.requestId())) {
            ctx.close();
            return;
        }

        requestManager.offer(request.requestId());
        if (!sessionManager.isValidSession(request.session())) {
            sessionManager.invalidateSession(request.session());
            log.info("Invalid session id: {}", request.session());
            var response =
                    Response.buildResponse(
                            request, Status.ERROR, new ErrorResponse("error.session.invalid"));
            ctx.channel().writeAndFlush(response);
            ctx.close();
            return;
        }

        handlers.stream()
                .filter(handler -> handler.isSupported(request))
                .findFirst()
                .ifPresentOrElse(
                        handler -> executeCommand(ctx, request, handler),
                        () -> processUnsupportedCommand(ctx, request));
    }

    private void processUnsupportedCommand(ChannelHandlerContext ctx, Request request) {
        var channel = ctx.channel();
        defaultCommandHandler.execute(request, channel);
    }

    private void executeCommand(
            ChannelHandlerContext ctx, Request request, CommandHandler handler) {
        var channel = ctx.channel();
        log.debug("Handling {} with {}", request.command(), handler.getClass().getSimpleName());
        handler.execute(request, channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
            return;
        }
        log.warn(cause.getMessage(), cause);
    }
}
