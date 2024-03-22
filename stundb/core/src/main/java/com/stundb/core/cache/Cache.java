package com.stundb.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Cache<T> {
    Boolean put(String key, T value);

    Boolean put(String key, T value, Long ttl);

    Optional<T> get(String key);

    Collection<T> getAll();

    Collection<String> retrieveKeysOfExpiredEntries();

    Map<String, T> dump();

    Boolean del(String key);

    Integer capacity();

    Integer size();

    Boolean isEmpty();

    void clear();
}
