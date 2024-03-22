package com.stundb.api.btree;

import lombok.Data;

import java.time.Instant;

@Data
public class Node<K extends Comparable<K>, V> {
    private K key;
    private V value;
    private Instant created;
    private Long ttl;
    private Node<K, V> left;
    private Node<K, V> right;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Node(K key, V value, Long ttl) {
        this.key = key;
        this.value = value;
        this.created = ttl != null ? Instant.now() : null;
        this.ttl = ttl;
    }

    public boolean isExpired() {
        if (created == null || ttl == null) {
            return false;
        }
        return Instant.now().isAfter(created.plusMillis(ttl));
    }
}
