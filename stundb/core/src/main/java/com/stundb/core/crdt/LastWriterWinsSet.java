package com.stundb.core.crdt;

import com.stundb.api.crdt.Entry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class LastWriterWinsSet implements CRDT<LastWriterWinsSet> {

    private final Set<Entry> add;
    private final Set<Entry> remove;

    public LastWriterWinsSet() {
        add = new ConcurrentSet<>();
        remove = new ConcurrentSet<>();
    }

    @Override
    public void merge(LastWriterWinsSet anotherLwwSet) {
        add.addAll(anotherLwwSet.getAdd());
        remove.addAll(anotherLwwSet.getRemove());
    }

    @Override
    public void add(Entry entry) {
        add.add(entry);
    }

    @Override
    public void remove(Entry entry) {
        remove.add(entry);
    }

    @Override
    public LastWriterWinsSet diff(LastWriterWinsSet other) {
        return new LastWriterWinsSet(diff(add, other.getAdd()), diff(remove, other.getRemove()));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var other = (LastWriterWinsSet) object;
        return add.equals(other.getAdd()) && remove.equals(other.getRemove());
    }

    @Override
    public int hashCode() {
        return 31 * add.hashCode() + remove.hashCode();
    }

    private Set<Entry> diff(Set<Entry> firstSet, final Set<Entry> secondSet) {
        return filtered(firstSet, element -> !secondSet.contains(element));
    }

    private Set<Entry> filtered(Set<Entry> set, Predicate<Entry> predicate) {
        return set.stream().filter(predicate).collect(Collectors.toSet());
    }
}
