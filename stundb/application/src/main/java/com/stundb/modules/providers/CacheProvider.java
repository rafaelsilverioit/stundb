package com.stundb.modules.providers;

import static java.util.Optional.ofNullable;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Capacity;
import com.stundb.core.cache.Cache;
import com.stundb.core.cache.FIFOCache;

import java.util.function.Function;

interface CacheProvider<T> {
    default Cache<T> getInstance(
            Cache<T> cache,
            ApplicationConfig config,
            Function<Capacity, Integer> transformer,
            Integer defaultCapacity) {
        return ofNullable(cache)
                .orElseGet(
                        () ->
                                new FIFOCache<>(
                                        ofNullable(config.getCapacity())
                                                .map(transformer)
                                                .orElse(defaultCapacity)));
    }
}
