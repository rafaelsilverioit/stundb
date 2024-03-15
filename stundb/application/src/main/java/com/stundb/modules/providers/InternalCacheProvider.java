package com.stundb.modules.providers;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Capacity;
import com.stundb.core.cache.Cache;
import com.stundb.net.core.models.Node;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class InternalCacheProvider implements Provider<Cache<Node>>, CacheProvider<Node> {

    @Inject private ApplicationConfig config;

    private Cache<Node> cache;

    @Override
    public Cache<Node> get() {
        cache = getInstance(cache, config, Capacity::internalCache, 10);
        return cache;
    }
}
