package com.stundb.acceptance.tests.modules;

import com.google.inject.AbstractModule;
import com.stundb.acceptance.tests.modules.providers.PropertiesProvider;
import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.net.client.modules.ClientModule;

import java.util.Properties;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Properties.class).toProvider(PropertiesProvider.class);
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());

        install(new ClientModule());
    }
}
