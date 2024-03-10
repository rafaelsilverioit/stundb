package com.stundb.api.providers;

import com.stundb.api.configuration.ConfigurationLoader;
import com.stundb.api.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.io.IOException;

public class ApplicationConfigProvider implements Provider<ApplicationConfig> {

    @Inject private ConfigurationLoader loader;

    @Override
    public ApplicationConfig get() {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
    }
}
