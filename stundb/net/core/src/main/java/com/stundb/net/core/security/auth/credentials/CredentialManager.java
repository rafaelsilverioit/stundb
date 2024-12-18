package com.stundb.net.core.security.auth.credentials;

import com.stundb.net.core.models.auth.ScramCredential;

import java.io.IOException;

public interface CredentialManager {

    void loadFromDisk() throws IOException;

    void add(String username, ScramCredential credential);

    ScramCredential get(String username);

    void remove(String username);

    boolean isEmpty();
}
