package com.stundb.modules;

import com.stundb.configuration.ConfigurationLoader;
import com.stundb.models.ApplicationConfig;

import javax.inject.Inject;
import javax.inject.Provider;

class ApplicationConfigProvider implements Provider<ApplicationConfig> {

    @Inject
    private ConfigurationLoader loader;

    @Override
    public ApplicationConfig get() {
        return loader.load();
    }
}
