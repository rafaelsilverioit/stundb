package com.stundb.modules;

import com.stundb.models.ApplicationConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestProvider implements Provider<MessageDigest> {

    @Inject
    private ApplicationConfig config;

    @Override
    public MessageDigest get() {
        try {
            return MessageDigest.getInstance(config.getDigestAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
