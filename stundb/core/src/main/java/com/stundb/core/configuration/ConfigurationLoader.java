package com.stundb.core.configuration;

import com.stundb.core.mappers.ApplicationConfigMapper;
import com.stundb.core.models.ApplicationConfig;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;

public class ConfigurationLoader {

    @Inject
    private Yaml yaml;

    @Inject
    private ApplicationConfigMapper mapper;

    public ApplicationConfig load() {
        var resource = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("application.yml");
        var properties = yaml.loadAs(resource, ApplicationProperties.class);
        return mapper.map(properties);
    }
}
