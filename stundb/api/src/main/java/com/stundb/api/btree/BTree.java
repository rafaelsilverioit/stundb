package com.stundb.api.btree;

import java.util.Collection;
import java.util.Optional;

public interface BTree<K extends Comparable<K>, V> {

    void upsert(K key, V value, Long ttl);

    Optional<Node<K, V>> find(K key);

    Collection<Node<K, V>> findAll();

    Collection<K> retrieveKeysOfExpiredEntries();

    void remove(K key);

    void clear();

    Integer size();
}
