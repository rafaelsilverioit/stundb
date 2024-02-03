package com.stundb.server.handlers.nodes;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.NodeService;
import io.netty.channel.Channel;
import lombok.NoArgsConstructor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@NoArgsConstructor
public class StartElectionHandler implements CommandHandler {

    @Inject
    private NodeService nodes;

    @Override
    public boolean isSupported(Request request) {
        return Command.START_ELECTION.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        nodes.startElection(request);
        writeAndFlush(request, null, channel);
    }
}
