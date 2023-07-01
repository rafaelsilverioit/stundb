package com.stundb.modules.providers;

import com.stundb.configuration.ConfigurationLoader;
import com.stundb.models.ApplicationConfig;

import javax.inject.Inject;
import javax.inject.Provider;

public class ApplicationConfigProvider implements Provider<ApplicationConfig> {

    @Inject
    private ConfigurationLoader loader;

    @Override
    public ApplicationConfig get() {
        return loader.load();
    }
}
