package com.stundb;

import com.google.inject.Guice;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.modules.Module;
import com.stundb.server.TcpServerImpl;

public class ApplicationMain {

    public static void main(String[] args) {
        var injector = Guice.createInjector(new Module());
        injector.getInstance(TcpServerImpl.class).run();
    }
}
