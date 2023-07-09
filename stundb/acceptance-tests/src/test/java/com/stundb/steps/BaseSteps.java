package com.stundb.steps;

import com.google.inject.Guice;
import com.stundb.modules.Module;
import com.stundb.net.client.StunDBClient;

import java.util.Properties;

public abstract class BaseSteps {

    protected final StunDBClient client;

    protected final Properties properties;

    protected final String host;

    protected final Integer port;

    protected BaseSteps() {
        var injector = Guice.createInjector(new Module());
        client = injector.getInstance(StunDBClient.class);
        properties = injector.getInstance(Properties.class);

        host = properties.getProperty("app.host");
        port = Integer.parseInt(properties.getProperty("app.port"));
    }
}
