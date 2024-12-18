package com.stundb.net.core.security.auth.credentials;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.stundb.net.core.models.auth.ScramCredential;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor
public class ScramCredentialManager implements CredentialManager {

    private static final ConcurrentHashMap<String, ScramCredential> credentials =
            new ConcurrentHashMap<>();
    private static final CsvSchema schema =
            CsvSchema.builder()
                    .addColumn("username")
                    .addColumn("salt")
                    .addColumn("serverKey")
                    .addColumn("storedKey")
                    .addColumn("iterations")
                    .build();
    private static final String STUNDB_DIR = "/stundb";
    private static final String USER_HOME = "user.home";
    private static final String CONFIG = "/.config";
    private static final String FALLBACK_CONFIG_DIR =
            System.getProperty(USER_HOME) + CONFIG + STUNDB_DIR;
    private final com.fasterxml.jackson.dataformat.csv.CsvMapper mapper;

    @Override
    public void loadFromDisk() throws IOException {
        var configDir =
                Optional.ofNullable(System.getenv("XDG_CONFIG_HOME"))
                        .map(configHome -> configHome + STUNDB_DIR)
                        .orElse(FALLBACK_CONFIG_DIR);

        var usersFile = new File(configDir, "users");
        //noinspection ResultOfMethodCallIgnored
        usersFile.getParentFile().mkdirs();
        var created = usersFile.createNewFile();

        // File was just created, so nothing to read at this point.
        if (created) {
            return;
        }

        try (MappingIterator<Map<String, String>> data =
                mapper.readerForMapOf(String.class).with(schema).readValues(usersFile)) {
            while (data.hasNextValue()) {
                var row = data.nextValue();
                var username = row.get("username");
                var salt = row.get("salt");
                var serverKey = row.get("serverKey");
                var storedKey = row.get("storedKey");
                var iterations = row.get("iterations");
                if (username == null
                        || salt == null
                        || serverKey == null
                        || storedKey == null
                        || iterations == null
                        || credentials.containsKey(username)) {
                    continue;
                }
                add(
                        username,
                        ScramCredential.toScramCredential(
                                salt, serverKey, storedKey, Integer.parseInt(iterations)));
            }
        }
    }

    @Override
    public void add(String username, ScramCredential credential) {
        credentials.put(username, credential);
    }

    @Override
    public ScramCredential get(String username) {
        return Optional.ofNullable(username).map(credentials::get).orElse(null);
    }

    @Override
    public void remove(String username) {
        Optional.ofNullable(username).ifPresent(credentials::remove);
    }

    @Override
    public boolean isEmpty() {
        return credentials.isEmpty();
    }
}
