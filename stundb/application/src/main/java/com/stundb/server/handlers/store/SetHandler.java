package com.stundb.server.handlers.store;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.requests.SetRequest;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.StoreService;
import io.netty.channel.Channel;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@NoArgsConstructor
public class SetHandler implements CommandHandler {

    @Inject
    private StoreService store;

    @Override
    public boolean isSupported(Request request) {
        return Command.SET.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        store.set((SetRequest) request.payload());
        writeAndFlush(request, null, channel);
    }
}
