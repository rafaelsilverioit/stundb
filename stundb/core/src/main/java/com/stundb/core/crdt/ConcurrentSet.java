package com.stundb.core.crdt;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConcurrentSet<E> extends AbstractSet<E> implements Serializable {

    private final ConcurrentMap<E, Boolean> map;

    public ConcurrentSet() {
        this.map = new ConcurrentHashMap<>();
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return map.containsKey(o);
    }

    public boolean add(E o) {
        return map.putIfAbsent(o, Boolean.TRUE) == null;
    }

    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    public void clear() {
        map.clear();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }
}