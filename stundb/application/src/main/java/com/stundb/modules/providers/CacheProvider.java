package com.stundb.modules.providers;

import com.stundb.core.cache.Cache;
import com.stundb.core.cache.FIFOCache;
import com.stundb.core.models.ApplicationConfig;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class CacheProvider<T> implements Provider<Cache<T>> {

    private Cache<T> cache;

    @Inject
    private ApplicationConfig config;

    @Override
    public Cache<T> get() {
        return getInstance();
    }

    private Cache<T> getInstance() {
        if (cache == null) {
            cache = new FIFOCache<>(config.getCapacity().getPublicCache());
        }
        return cache;
    }
}
