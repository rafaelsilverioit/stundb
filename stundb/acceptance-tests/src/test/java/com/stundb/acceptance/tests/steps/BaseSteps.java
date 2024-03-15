package com.stundb.acceptance.tests.steps;

import com.google.inject.Guice;
import com.stundb.acceptance.tests.modules.Module;
import com.stundb.net.client.StunDBClient;

import java.util.Properties;

public abstract class BaseSteps {

    protected final StunDBClient client;

    protected final Properties properties;

    protected final String mainNodeIp;

    protected final Integer mainNodePort;

    protected final String secondaryNodeIp;

    protected final Integer secondaryNodePort;

    protected BaseSteps() {
        var injector = Guice.createInjector(new Module());
        client = injector.getInstance(StunDBClient.class);
        properties = injector.getInstance(Properties.class);

        mainNodeIp = properties.getProperty("main.node.host");
        mainNodePort = Integer.parseInt(properties.getProperty("main.node.port"));
        secondaryNodeIp = properties.getProperty("secondary.node.host");
        secondaryNodePort = Integer.parseInt(properties.getProperty("secondary.node.port"));
    }
}
