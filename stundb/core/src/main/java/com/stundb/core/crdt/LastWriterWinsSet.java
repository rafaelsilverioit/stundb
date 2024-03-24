package com.stundb.core.crdt;

import static java.util.function.Predicate.not;

import com.stundb.api.crdt.Entry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class LastWriterWinsSet implements CRDT {

    private final Set<Entry> added;
    private final Set<Entry> removed;

    public LastWriterWinsSet() {
        added = new ConcurrentSet<>();
        removed = new ConcurrentSet<>();
    }

    @Override
    public void merge(Collection<Entry> added, Collection<Entry> removed) {
        this.added.addAll(added);
        this.removed.addAll(removed);
    }

    @Override
    public void add(Entry entry) {
        added.add(entry);
    }

    @Override
    public void remove(Entry entry) {
        removed.add(entry);
    }

    @Override
    public CRDT diff(CRDT anotherState) {
        return new LastWriterWinsSet(
                diff(added, anotherState.getAdded()), diff(removed, anotherState.getRemoved()));
    }

    public Map<String, Long> versionClock() {
        return Stream.concat(added.stream(), removed.stream())
                .collect(Collectors.groupingBy(Entry::key, Collectors.counting()));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var other = (LastWriterWinsSet) object;
        return added.equals(other.getAdded()) && removed.equals(other.getRemoved());
    }

    @Override
    public int hashCode() {
        return 31 * added.hashCode() + removed.hashCode();
    }

    private Set<Entry> diff(Set<Entry> firstSet, final Set<Entry> secondSet) {
        return firstSet.stream().filter(not(secondSet::contains)).collect(Collectors.toSet());
    }
}
