package com.stundb.core.modules.providers;

import com.stundb.core.configuration.ConfigurationLoader;
import com.stundb.core.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ApplicationConfigProvider implements Provider<ApplicationConfig> {

    @Inject
    private ConfigurationLoader loader;

    @Override
    public ApplicationConfig get() {
        return loader.load();
    }
}
