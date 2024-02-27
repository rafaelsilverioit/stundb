package com.stundb.api.configuration;

import com.stundb.api.mappers.ApplicationConfigMapper;
import com.stundb.api.models.ApplicationConfig;

import jakarta.inject.Inject;

import org.yaml.snakeyaml.Yaml;

public class ConfigurationLoader {

    @Inject private Yaml yaml;
    @Inject private ApplicationConfigMapper mapper;

    public ApplicationConfig load() {
        var resource =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("application.yml");
        var properties = yaml.loadAs(resource, ApplicationProperties.class);
        return mapper.map(properties);
    }
}
