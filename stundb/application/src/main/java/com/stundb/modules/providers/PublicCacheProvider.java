package com.stundb.modules.providers;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Capacity;
import com.stundb.core.cache.Cache;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class PublicCacheProvider implements Provider<Cache<Object>>, CacheProvider<Object> {

    @Inject private ApplicationConfig config;

    private Cache<Object> cache;

    @Override
    public Cache<Object> get() {
        cache = getInstance(cache, config, Capacity::publicCache, 100);
        return cache;
    }
}
