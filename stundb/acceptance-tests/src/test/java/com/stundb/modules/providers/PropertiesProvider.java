package com.stundb.modules.providers;

import lombok.SneakyThrows;

import javax.inject.Provider;
import javax.inject.Singleton;
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
