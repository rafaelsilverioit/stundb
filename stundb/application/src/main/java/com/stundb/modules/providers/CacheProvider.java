package com.stundb.modules.providers;

import com.stundb.core.cache.BTreeCache;
import com.stundb.core.cache.Cache;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class CacheProvider<T> implements Provider<Cache<T>> {

    @Inject private BTreeCache<T> cache;

    @Override
    public Cache<T> get() {
        return cache;
    }
}
