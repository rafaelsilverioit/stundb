package com.stundb.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stundb.api.mappers.ApplicationConfigMapper;
import com.stundb.api.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.util.Optional;

public class ConfigurationLoader {

    @Inject private ObjectMapper yamlMapper;
    @Inject private Validator validator;
    @Inject private ApplicationConfigMapper mapper;

    public ApplicationConfig load() throws IOException {
        var configurationFile = System.getProperty("stundb.configuration.file", "application.yml");
        var resource =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(configurationFile);

        if (resource == null) {
            throw new IllegalArgumentException("Configuration file not found: %s".formatted(configurationFile));
        }

        var properties = yamlMapper.readValue(resource, ApplicationProperties.class);
        validate(properties);
        return mapper.map(properties);
    }

    private void validate(ApplicationProperties properties) {
        validator.validate(properties).stream()
                .map(ConstraintViolation::getMessage)
                .reduce("%s, %s"::formatted)
                .ifPresent(
                        message -> {
                            throw new IllegalArgumentException(
                                    "Invalid properties detected: [%s]".formatted(message));
                        });
    }
}
