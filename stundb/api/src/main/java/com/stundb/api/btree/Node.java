package com.stundb.api.btree;

import lombok.Data;

@Data
public class Node<K extends Comparable<K>, V> {
    private K key;
    private V value;
    private Node<K, V> left;
    private Node<K, V> right;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
