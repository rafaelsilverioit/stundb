package com.stundb;

import com.google.inject.Guice;
import com.stundb.modules.Module;
import com.stundb.server.GrpcServer;

import java.io.IOException;

public class ApplicationMain {

    public static void main(String[] args) throws IOException {
        var injector = Guice.createInjector(new Module());
        injector.getInstance(GrpcServer.class).run();
    }
}
