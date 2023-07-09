package com.stundb.core.cache;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Optional.ofNullable;

public record FIFOCache<T>(Integer capacity, Map<String, T> cache, ReentrantReadWriteLock lock)
        implements Cache<T> {

    public FIFOCache(Integer capacity) {
        this(capacity, new LinkedHashMap<>(capacity), new ReentrantReadWriteLock());
    }

    @Override
    public Boolean put(String key, T value) {
        if(hasReachedCapacityThreshold(key)) {
            var entry = cache.entrySet().iterator().next();
            del(entry.getKey());
        }

        this.lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public Boolean del(String key) {
        this.lock.writeLock().lock();
        try {
            cache.remove(key);
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<T> get(String key) {
        this.lock.readLock().lock();
        try {
            return ofNullable(cache.get(key));
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Collection<T> getAll() {
        // Needed due to serialization.
        return List.copyOf(dump().values());
    }

    @Override
    public Map<String, T> dump() {
        this.lock.readLock().lock();
        try {
            return cache;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Boolean isEmpty() {
        this.lock.readLock().lock();
        try {
            return cache.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        this.lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Integer size() {
        this.lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private Boolean hasReachedCapacityThreshold(String key) {
        this.lock.readLock().lock();
        try {
            // if the cache contains a key, we just override it :)
            return !cache.containsKey(key) && cache.size() == capacity;
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
