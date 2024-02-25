package com.stundb.server.handlers.nodes;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.NodeService;

import io.netty.channel.Channel;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor
public class ElectedHandler implements CommandHandler {

    @Inject private NodeService nodes;

    @Override
    public boolean isSupported(Request request) {
        return Command.ELECTED.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        nodes.elected((ElectedRequest) request.payload());
        writeAndFlush(request, null, channel);
    }
}
