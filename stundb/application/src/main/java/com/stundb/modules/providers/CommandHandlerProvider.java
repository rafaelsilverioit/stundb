package com.stundb.modules.providers;

import com.google.inject.Injector;
import com.stundb.net.server.handlers.CommandHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.ServiceLoader;

@Singleton
public class CommandHandlerProvider implements Provider<List<? extends CommandHandler>> {

    @Inject
    private Injector injector;

    @Override
    public List<? extends CommandHandler> get() {
        return ServiceLoader.load(CommandHandler.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .map(injector::getInstance)
                .toList();
    }
}
