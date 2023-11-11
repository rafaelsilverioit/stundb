package com.stundb.server.handlers.store;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.ExistsRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.StoreService;
import io.netty.channel.Channel;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@NoArgsConstructor
public class ExistsHandler implements CommandHandler {

    @Inject
    private StoreService store;

    @Override
    public boolean isSupported(Request request) {
        return Command.EXISTS.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        var data = store.exists((ExistsRequest) request.payload());
        writeAndFlush(request, data, channel);
    }
}
