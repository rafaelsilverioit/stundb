package com.stundb.modules;

import com.google.inject.AbstractModule;
import com.stundb.core.configuration.ConfigurationLoader;
import com.stundb.core.mappers.ApplicationConfigMapper;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.modules.providers.ApplicationConfigProvider;
import com.stundb.modules.providers.PropertiesProvider;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.modules.providers.CodecProvider;

import java.util.Properties;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Codec.class).toProvider(CodecProvider.class).asEagerSingleton();

        bind(Properties.class).toProvider(PropertiesProvider.class);
        bind(ConfigurationLoader.class).toInstance(new ConfigurationLoader());
        bind(ApplicationConfig.class).toProvider(ApplicationConfigProvider.class);
        bind(ApplicationConfigMapper.class).toInstance(ApplicationConfigMapper.INSTANCE);

        install(new ClientModule());
    }
}
