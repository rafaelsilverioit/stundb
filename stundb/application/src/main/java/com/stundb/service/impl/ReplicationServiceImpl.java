package com.stundb.service.impl;

import static com.stundb.net.core.models.NodeStatus.State.FAILING;
import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import com.stundb.api.crdt.Entry;
import com.stundb.api.models.Tuple;
import com.stundb.core.cache.Cache;
import com.stundb.core.crdt.CRDT;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Singleton
public class ReplicationServiceImpl implements ReplicationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private CRDT state;
    @Inject private StunDBClient client;
    @Inject private Cache<Object> cache;
    @Inject private Cache<Node> internalCache;
    @Inject private UniqueId uniqueId;
    @Inject private NodeUtils utils;

    /* TODO: think about partial replication/replication on demand instead of replicating every action received
     *       e.g: node x is 40 state entries behind node y, so node y sends these 40 state entries for x to sync
     *       e.g: node x suddenly decides its state is corrupted, clears its state and asks for full synchronization
     */

    @Loggable
    @Override
    public void synchronize(Collection<Entry> added, Collection<Entry> removed) {
        state.merge(added, removed);

        addToCache(added, state.getRemoved());
        removeFromCache(removed, state.getAdded());
    }

    @Loggable
    @Override
    public Tuple<Collection<Entry>, Collection<Entry>> generateStateSnapshot() {
        return generateStateFrom(Instant.MIN);
    }

    @Override
    public void add(String key, Object value) {
        var entry = buildEntry(key, value);
        state.add(entry);
        replicate(entry.timestamp());
    }

    @Override
    public void remove(String key) {
        var entry = buildEntry(key, null);
        state.remove(entry);
        replicate(entry.timestamp());
    }

    @Override
    public Tuple<Collection<Entry>, Collection<Entry>> verifySynchroneity(
            Map<String, Long> theirClock) {
        var ourClock = state.versionClock();

        if (Objects.equals(ourClock, theirClock)) {
            return new Tuple<>(List.of(), List.of());
        }

        var delta = new ArrayList<String>();

        ourClock.forEach(
                (ourKey, ourValue) -> {
                    var theirValue = theirClock.get(ourKey);
                    if (theirValue == null || ourValue > theirValue) {
                        delta.add(ourKey);
                    }
                });

        var add = filterEntries(state.getAdded(), delta);
        var remove = filterEntries(state.getRemoved(), delta);

        return new Tuple<>(add, remove);
    }

    @Override
    public Map<String, Long> generateVersionClock() {
        return state.versionClock();
    }

    private void replicate(Instant from) {
        var data = generateStateFrom(from);
        var request =
                Request.buildRequest(
                        Command.SYNCHRONIZE, new CRDTRequest(data.left(), data.right()));
        utils.filterNodesByState(internalCache.getAll(), uniqueId.number(), List.of(RUNNING))
                .filter(Node::leader)
                .forEach(node -> synchronizeWithLeaderNode(node, request));
    }

    private void synchronizeWithLeaderNode(Node node, Request request) {
        client.requestAsync(request, node.ip(), node.port())
                .handle(
                        (response, error) -> {
                            if (error != null) {
                                logger.error("Request failed", error);
                                internalCache.upsert(
                                        node.uniqueId().toString(), node.clone(FAILING));
                            }
                            return response;
                        });
    }

    private Entry buildEntry(String key, Object value) {
        return new Entry(Instant.now(), key, value);
    }

    private Tuple<Collection<Entry>, Collection<Entry>> generateStateFrom(Instant from) {
        var add = filterEntries(state.getAdded(), from);
        var remove = filterEntries(state.getRemoved(), from);
        return new Tuple<>(add, remove);
    }

    private List<Entry> filterEntries(Set<Entry> entries, Instant from) {
        return entries.stream().filter(e -> !e.timestamp().isBefore(from)).toList();
    }

    private List<Entry> filterEntries(Set<Entry> entries, List<String> keys) {
        return entries.stream().filter(e -> keys.contains(e.key())).toList();
    }

    private void addToCache(Collection<Entry> synchronizedEntries, Set<Entry> stateEntries) {
        // adds items to the cache if they weren't already added and then removed
        updateCache(
                synchronizedEntries,
                stateEntries,
                this::shouldEntryBeAdded,
                e -> cache.upsert(e.key(), e.value()));
    }

    private void removeFromCache(Collection<Entry> synchronizedEntries, Set<Entry> stateEntries) {
        // removes items from the cache if they weren't already removed and then added back again
        updateCache(
                synchronizedEntries,
                stateEntries,
                this::shouldEntryBeRemoved,
                e -> cache.del(e.key()));
    }

    private void updateCache(
            Collection<Entry> synchronizedEntries,
            Set<Entry> updatedEntries,
            BiPredicate<Entry, Set<Entry>> predicate,
            Consumer<Entry> consumer) {
        synchronizedEntries.stream()
                .filter(entry -> predicate.test(entry, updatedEntries))
                .forEach(consumer);
    }

    private boolean shouldEntryBeAdded(Entry entryToBeAdded, Set<Entry> removed) {
        return removed.stream().noneMatch(target -> hasEntryBeenRemoved(entryToBeAdded, target));
    }

    private boolean shouldEntryBeRemoved(Entry entryToBeRemoved, Set<Entry> added) {
        return added.stream().noneMatch(target -> hasEntryBeenReAdded(entryToBeRemoved, target));
    }

    private boolean hasEntryBeenRemoved(Entry entryToBeAdded, Entry target) {
        return Objects.equals(target.key(), entryToBeAdded.key())
                && target.timestamp().isAfter(entryToBeAdded.timestamp());
    }

    private boolean hasEntryBeenReAdded(Entry entryToBeRemoved, Entry target) {
        return Objects.equals(target.key(), entryToBeRemoved.key())
                && !target.timestamp().isBefore(entryToBeRemoved.timestamp());
    }
}
