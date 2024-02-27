package com.stundb.api.providers;

import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.api.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ApplicationConfigProvider implements Provider<ApplicationConfig> {

    @Inject private ConfigurationLoader loader;

    @Override
    public ApplicationConfig get() {
        return loader.load();
    }
}
