package com.stundb.net.server.handlers;

import com.stundb.core.logging.Loggable;
import com.stundb.net.core.models.requests.Request;
import io.netty.channel.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ChannelHandler.Sharable
@AllArgsConstructor
public class RequestHandler extends SimpleChannelInboundHandler<Request> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<? extends CommandHandler> handlers;

    private DefaultCommandHandler defaultCommandHandler;

    @Loggable
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) {
        handlers.stream()
                .filter(runner -> runner.isSupported(request))
                .findFirst()
                .ifPresentOrElse(
                        runner -> executeCommand(ctx, request, runner),
                        () -> processUnsupportedCommand(ctx, request));
    }

    private void processUnsupportedCommand(ChannelHandlerContext ctx, Request request) {
        var channel = ctx.channel();
        defaultCommandHandler.execute(request, channel);
        closeChannel(channel);
    }

    private void executeCommand(ChannelHandlerContext ctx, Request request, CommandHandler runner) {
        var channel = ctx.channel();
        runner.execute(request, channel);
        closeChannel(channel);
    }

    private void closeChannel(Channel channel) {
        logger.debug("----> [" + channel.id() + "] reply sent!");
        channel.close().addListener((ChannelFutureListener) future -> {
            future.awaitUninterruptibly();
            logger.debug("----> [" + channel.id() + "] disconnected");
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
