package com.stundb.core.modules.providers;

import com.stundb.core.configuration.ConfigurationLoader;
import com.stundb.core.models.ApplicationConfig;

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
