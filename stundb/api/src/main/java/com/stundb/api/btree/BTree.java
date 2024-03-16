package com.stundb.api.btree;

import java.util.Collection;
import java.util.Optional;

public interface BTree<K extends Comparable<K>, V> {

    void putIfAbsent(K key, V value);

    Optional<Node<K, V>> find(K key);

    Collection<Node<K, V>> findAll();

    void remove(K key);

    void clear();

    Integer size();
}
