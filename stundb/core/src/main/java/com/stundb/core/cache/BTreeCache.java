package com.stundb.core.cache;

import com.stundb.api.btree.BTree;
import com.stundb.api.btree.Node;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BTreeCache<V> implements Cache<V> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    @Inject private BTree<String, V> tree;

    @Override
    public Boolean put(String key, V value) {
        return put(key, value, null);
    }

    @Override
    public Boolean put(String key, V value, Long ttl) {
        get(key).ifPresentOrElse(
                        __ ->
                                writeLock(key, value, (k, v) -> {
                                    tree.remove(k);
                                    tree.putIfAbsent(k, v, ttl);
                                    return null;
                                }),
                        () ->
                                writeLock(key, value, (k, v) -> {
                                    tree.putIfAbsent(k, v, ttl);
                                    return null;
                                }));
        return true;
    }

    @Override
    public Optional<V> get(String key) {
        return readLock(key, (k) -> tree.find(k)).map(Node::getValue);
    }

    @Override
    public Collection<V> getAll() {
        return readLock(null, (__) -> tree.findAll()).stream()
                .map(Node::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> retrieveKeysOfExpiredEntries() {
        return readLock(null, (__) -> tree.retrieveKeysOfExpiredEntries());
    }

    @Override
    public Map<String, V> dump() {
        return readLock(null, (__) -> tree.findAll()).stream()
                .collect(Collectors.toMap(Node::getKey, Node::getValue));
    }

    @Override
    public Boolean del(String key) {
        writeLock(key, null, (k, __) -> {
            tree.remove(k);
            return null;
        });
        return true;
    }

    @Override
    public Integer capacity() {
        // TODO: think of handling fifo on a btree
        return -1;
    }

    @Override
    public Integer size() {
        return readLock(null, (__) -> tree.size());
    }

    @Override
    public Boolean isEmpty() {
        return readLock(null, (__) -> tree.size() == 0);
    }

    @Override
    public void clear() {
        writeLock(null, null, (k, __) -> {
            tree.clear();
            return null;
        });
    }

    private <T> T readLock(String key, Function<String, T> fn) {
        this.lock.readLock().lock();
        try {
            return fn.apply(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void writeLock(String key, V value, BiFunction<String, V, Void> fn) {
        this.lock.writeLock().lock();
        try {
            fn.apply(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
