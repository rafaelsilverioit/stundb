package com.stundb.api.btree;

import java.util.*;

public class BTreeImpl<K extends Comparable<K>, V> implements BTree<K, V> {

    private Node<K, V> root;

    @Override
    public void putIfAbsent(K key, V value) {
        if (root == null) {
            root = new Node<>(key, value);
            return;
        }

        var node = find(key, root);
        if (node.isPresent()) {
            return;
        }

        var target = root;

        while (target != null) {
            if (key.compareTo(target.getKey()) > 0) {
                if (target.getRight() == null) {
                    target.setRight(new Node<>(key, value));
                    return;
                }

                target = target.getRight();
                continue;
            }

            if (target.getLeft() == null) {
                target.setLeft(new Node<>(key, value));
                return;
            }

            target = target.getLeft();
        }
    }

    @Override
    public Optional<Node<K, V>> find(K key) {
        return find(key, root);
    }

    @Override
    public Collection<Node<K, V>> findAll() {
        return getNodeValue(root);
    }

    @Override
    public void remove(K key) {
        root = remove(key, root);
    }

    @Override
    public void clear() {
        root = clear(root);
    }

    @Override
    public Integer size() {
        return count(root);
    }

    private Collection<Node<K, V>> getNodeValue(Node<K, V> node) {
        if (node == null) {
            return new ArrayList<>();
        }

        if (node.getLeft() == null && node.getRight() == null) {
            return new ArrayList<>(List.of(node));
        }

        if (node.getLeft() == null) {
            var arr = getNodeValue(node.getRight());
            arr.add(node);
            return arr;
        }

        if (node.getRight() == null) {
            var arr = getNodeValue(node.getLeft());
            arr.add(node);
            return arr;
        }

        var result = new ArrayList<>(getNodeValue(node.getLeft()));
        result.addAll(getNodeValue(node.getRight()));
        result.add(node);
        return result;
    }

    private Optional<Node<K, V>> find(K key, Node<K, V> target) {
        if (target == null) {
            return Optional.empty();
        }

        // search finished
        if (key.compareTo(target.getKey()) == 0) {
            return Optional.of(target);
        }

        // key is most probably on the right side of the tree
        if (key.compareTo(target.getKey()) > 0) {
            return find(key, target.getRight());
        }

        // key is most probably on the left side of the tree
        return find(key, target.getLeft());
    }

    private Node<K, V> remove(K key, Node<K, V> target) {
        if (target == null) {
            return null;
        }

        // key < 0, then remove left child
        if (key.compareTo(target.getKey()) < 0) {
            target.setLeft(remove(key, target.getLeft()));
            return target;
        }

        // key < 0, then remove right child
        if (key.compareTo(target.getKey()) > 0) {
            target.setRight(remove(key, target.getRight()));
            return target;
        }

        // if no children, then just remove the node
        if (target.getLeft() == null && target.getRight() == null) {
            return null;
        }

        // if left child is present, then replace parent with child
        if (target.getLeft() == null) {
            target = target.getRight();
            return target;
        }

        // if right child is present, then replace parent with child
        if (target.getRight() == null) {
            target = target.getLeft();
            return target;
        }

        // in case of both children are present, replace with the minimum key
        var min = min(target.getRight());
        target.setKey(min);
        target.setRight(remove(min, target.getRight()));
        return target;
    }

    private K min(Node<K, V> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node.getKey();
    }

    private Node<K, V> clear(Node<K, V> target) {
        if (target != null) {
            clear(target.getRight());
            clear(target.getLeft());
        }
        return null;
    }

    private Integer count(Node<K, V> target) {
        if (target != null) {
            return 1 + count(target.getRight()) + count(target.getLeft());
        }
        return 0;
    }
}
