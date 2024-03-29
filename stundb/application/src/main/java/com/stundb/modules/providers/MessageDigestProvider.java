package com.stundb.modules.providers;

import com.stundb.api.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestProvider implements Provider<MessageDigest> {

    @Inject private ApplicationConfig config;

    @Override
    public MessageDigest get() {
        try {
            return MessageDigest.getInstance(config.digestAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
