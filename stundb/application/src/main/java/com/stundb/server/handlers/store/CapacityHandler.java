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
public class CapacityHandler implements CommandHandler {

    @Inject private StoreService store;

    @Override
    public boolean isSupported(Request request) {
        return Command.CAPACITY.equals(request.command());
    }

    @Override
    public void execute(Request request, Channel channel) {
        writeAndFlush(request, store.capacity(), channel);
    }
}
