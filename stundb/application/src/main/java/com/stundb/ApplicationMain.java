package com.stundb;

import com.google.inject.Guice;
import com.stundb.modules.Module;
import com.stundb.net.core.security.auth.ScramSaslClientProvider;
import com.stundb.net.core.security.auth.ScramSaslServerProvider;
import com.stundb.net.server.TcpServer;

import java.util.Optional;

public class ApplicationMain {

    static {
        ScramSaslClientProvider.initialize();
        ScramSaslServerProvider.initialize();
    }

    public static void main(String[] args) {
        var configUrl = ApplicationMain.class.getClassLoader().getResource("jaas.config");
        Optional.ofNullable(configUrl)
                .ifPresentOrElse(
                        url ->
                                System.setProperty(
                                        "java.security.auth.login.config", url.toExternalForm()),
                        () -> {
                            throw new RuntimeException("JAAS config file not found in resources!");
                        });

        var injector = Guice.createInjector(new Module());
        injector.getInstance(TcpServer.class).run();
    }
}
