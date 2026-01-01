package com.stundb.core.cache;

import com.stundb.api.btree.BTree;
import com.stundb.api.btree.Node;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BTreeCache<V> implements Cache<V> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    @Inject private BTree<String, V> tree;

    @Override
    public Boolean upsert(String key, V value) {
        return upsert(key, value, null);
    }

    @Override
    public Boolean upsert(String key, V value, Long ttl) {
        writeLock(key, value, (k, v) -> tree.upsert(k, v, ttl));
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
    public Boolean del(String key, Consumer<V> cleanUp) {
        BiConsumer<String, V> consumer =
                (k, v) -> {
                    tree.remove(k);
                    Optional.ofNullable(v).ifPresent(cleanUp);
                };

        var value = tree.find(key)
                .map(Node::getValue)
                .orElse(null);
        writeLock(key, value, consumer);
        return true;
    }

    @Override
    public Boolean del(String key) {
        writeLock(key, null, (k, __) -> tree.remove(k));
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
        writeLock(null, null, (k, __) -> tree.clear());
    }

    private <T> T readLock(String key, Function<String, T> fn) {
        this.lock.readLock().lock();
        try {
            return fn.apply(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void writeLock(String key, V value, BiConsumer<String, V> fn) {
        this.lock.writeLock().lock();
        try {
            fn.accept(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
