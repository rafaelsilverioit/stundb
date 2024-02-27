package com.stundb.modules.providers;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.cache.Cache;
import com.stundb.core.cache.FIFOCache;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class CacheProvider<T> implements Provider<Cache<T>> {

    @Inject private ApplicationConfig config;

    private Cache<T> cache;

    @Override
    public Cache<T> get() {
        return getInstance();
    }

    private Cache<T> getInstance() {
        if (cache == null) {
            cache = new FIFOCache<>(config.getCapacity().publicCache());
        }
        return cache;
    }
}
