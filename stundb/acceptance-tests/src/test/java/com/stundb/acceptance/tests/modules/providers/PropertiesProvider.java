package com.stundb.acceptance.tests.modules.providers;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.util.Properties;

@Singleton
public class PropertiesProvider implements Provider<Properties> {

    @SneakyThrows
    @Override
    public Properties get() {
        try (var inputStream = new FileInputStream("src/test/resources/test.properties")) {
            var properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
