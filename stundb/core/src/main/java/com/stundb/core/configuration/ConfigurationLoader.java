package com.stundb.core.configuration;

import com.stundb.core.mappers.ApplicationConfigMapper;
import com.stundb.core.models.ApplicationConfig;

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
