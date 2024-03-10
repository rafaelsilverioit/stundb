package com.stundb.acceptance.tests.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.stundb.acceptance.tests.modules.providers.PropertiesProvider;
import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.api.mappers.ApplicationConfigMapper;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.providers.ApplicationConfigProvider;
import com.stundb.net.client.modules.ClientModule;

import java.util.Properties;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Properties.class).toProvider(PropertiesProvider.class);
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(ApplicationConfig.class)
                .toProvider(ApplicationConfigProvider.class)
                .in(Singleton.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);

        install(new ClientModule());
    }
}
