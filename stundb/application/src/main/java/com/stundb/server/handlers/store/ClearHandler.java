package com.stundb.server.handlers.store;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.StoreService;

import io.netty.channel.Channel;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor
public class ClearHandler implements CommandHandler {

    @Inject private StoreService store;

    @Override
    public boolean isSupported(Request request) {
        return Command.CLEAR.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        store.clear();
        writeAndFlush(request, null, channel);
    }
}
