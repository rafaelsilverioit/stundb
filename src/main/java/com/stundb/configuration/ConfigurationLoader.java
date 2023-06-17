package com.stundb.configuration;

import com.stundb.mappers.ApplicationConfigMapper;
import com.stundb.models.ApplicationConfig;
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
