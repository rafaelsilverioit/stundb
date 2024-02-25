package com.stundb.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Cache<T> {
    Boolean put(String key, T value);

    Optional<T> get(String key);

    Collection<T> getAll();

    Map<String, T> dump();

    Boolean del(String key);

    Integer capacity();

    Integer size();

    Boolean isEmpty();

    void clear();
}
