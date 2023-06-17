package com.stundb.utils;

import javax.inject.Inject;
import java.security.MessageDigest;

public class NodeUtils {

    @Inject
    private MessageDigest digester;

    public long generateUniqueId(String key) {
        digester.reset();
        digester.update(key.getBytes());

        byte[] digest = digester.digest();

        long hash = 0;
        for (int i = 0; i < 4; i++) {
            hash <<= 8;
            hash |= ((int) digest[i]) & 0xFF;
        }

        return hash;
    }
}
