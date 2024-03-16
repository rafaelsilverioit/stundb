package com.stundb.core.cache;

import com.stundb.api.btree.BTree;
import com.stundb.api.btree.Node;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BTreeCache<V> implements Cache<V> {

    @Inject private BTree<String, V> tree;

    // TODO: handle synchronization

    @Override
    public Boolean put(String key, V value) {
        tree.find(key)
                .ifPresentOrElse(
                        __ -> {
                            tree.remove(key);
                            tree.putIfAbsent(key, value);
                        },
                        () -> tree.putIfAbsent(key, value));
        return true;
    }

    @Override
    public Optional<V> get(String key) {
        return tree.find(key).map(Node::getValue);
    }

    @Override
    public Collection<V> getAll() {
        return tree.findAll().stream().map(Node::getValue).collect(Collectors.toList());
    }

    @Override
    public Map<String, V> dump() {
        return tree.findAll().stream().collect(Collectors.toMap(Node::getKey, Node::getValue));
    }

    @Override
    public Boolean del(String key) {
        tree.remove(key);
        return true;
    }

    @Override
    public Integer capacity() {
        // TODO: think of handling fifo on a btree
        return -1;
    }

    @Override
    public Integer size() {
        return tree.size();
    }

    @Override
    public Boolean isEmpty() {
        return tree.size() == 0;
    }

    @Override
    public void clear() {
        tree.clear();
    }
}
