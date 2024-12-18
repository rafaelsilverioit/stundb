package com.stundb.net.core.modules.providers;

import com.stundb.net.core.annotations.CsvMapper;
import com.stundb.net.core.security.auth.credentials.CredentialManager;
import com.stundb.net.core.security.auth.credentials.ScramCredentialManager;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Singleton
public class CredentialManagerProvider implements Provider<CredentialManager> {

    private CredentialManager credentialManager;

    @Inject @CsvMapper private com.fasterxml.jackson.dataformat.csv.CsvMapper mapper;

    @Override
    public CredentialManager get() {
        if (credentialManager == null) {
            credentialManager = new ScramCredentialManager(mapper);
            try {
                credentialManager.loadFromDisk();
            } catch (IOException e) {
                log.warn("Failed to load credentials from disk", e);
            }
        }
        return credentialManager;
    }
}
