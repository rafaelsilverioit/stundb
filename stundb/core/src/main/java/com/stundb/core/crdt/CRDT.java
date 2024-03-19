package com.stundb.core.crdt;

import com.stundb.api.crdt.Entry;

import java.util.Collection;
import java.util.Set;

public interface CRDT {

    void merge(Collection<Entry> added, Collection<Entry> removed);

    void add(Entry data);

    void remove(Entry data);

    Set<Entry> getAdded();

    Set<Entry> getRemoved();

    CRDT diff(CRDT other);
}
