package com.stundb.api.providers;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import jakarta.inject.Provider;

public class ObjectMapperProvider implements Provider<ObjectMapper> {

    private ObjectMapper mapper;

    @Override
    public ObjectMapper get() {
        mapper =
                ofNullable(mapper)
                        .orElseGet(
                                () ->
                                        new ObjectMapper(new YAMLFactory())
                                                .configure(
                                                        DeserializationFeature
                                                                .FAIL_ON_UNKNOWN_PROPERTIES,
                                                        false));
        return mapper;
    }
}
