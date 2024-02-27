package com.stundb.core.crdt;

import com.stundb.api.crdt.Entry;

import java.io.Serializable;

public interface CRDT<T extends CRDT<T>> extends Serializable {

    void merge(T other);

    void add(Entry data);

    void remove(Entry data);

    T diff(T other);
}
