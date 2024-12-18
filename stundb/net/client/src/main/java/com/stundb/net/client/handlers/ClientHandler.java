package com.stundb.net.client.handlers;

import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
public class ClientHandler extends SimpleChannelInboundHandler<Response> {

    private final CompletableFuture<Response> promise;

    private final Request request;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Response response) {
        promise.complete(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        promise.completeExceptionally(cause);
        if (cause instanceof ReadTimeoutException) {
            return;
        }
        log.warn(cause.getMessage(), cause);
    }
}
