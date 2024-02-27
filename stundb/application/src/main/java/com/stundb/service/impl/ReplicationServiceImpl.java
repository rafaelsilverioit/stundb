package com.stundb.service.impl;

import static com.stundb.net.core.models.NodeStatus.State.FAILING;
import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import com.stundb.api.crdt.Entry;
import com.stundb.core.cache.Cache;
import com.stundb.core.crdt.LastWriterWinsSet;
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
import java.util.List;

@Singleton
public class ReplicationServiceImpl implements ReplicationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final LastWriterWinsSet state = new LastWriterWinsSet();

    @Inject private StunDBClient client;
    @Inject private Cache<Object> cache;
    @Inject private Cache<Node> internalCache;
    @Inject private UniqueId uniqueId;
    @Inject private NodeUtils utils;

    @Loggable
    public void synchronize(CRDTRequest request) {
        var lww = new LastWriterWinsSet();
        var add = request.added();
        var remove = request.removed();

        lww.getAdd().addAll(add);
        lww.getRemove().addAll(remove);

        state.merge(lww);

        var added = state.getAdd();
        var removed = state.getRemove();

        add.stream()
                .filter(
                        e ->
                                removed.stream()
                                        .noneMatch(
                                                e2 ->
                                                        e2.key().equals(e.key())
                                                                && e2.timestamp()
                                                                        .isAfter(e.timestamp())))
                .forEach(e -> cache.put(e.key(), e.value()));

        remove.stream()
                .filter(
                        e ->
                                added.stream()
                                        .noneMatch(
                                                e2 ->
                                                        e2.key().equals(e.key())
                                                                && !e2.timestamp()
                                                                        .isBefore(e.timestamp())))
                .forEach(
                        e -> {
                            cache.del(e.key());
                            added.stream()
                                    .filter(
                                            entry ->
                                                    e.key().equals(entry.key())
                                                            && e.timestamp()
                                                                    .isAfter(entry.timestamp()))
                                    .forEach(
                                            entry -> {
                                                added.remove(entry);
                                                added.add(entry.cloneRemovingValue());
                                            });
                        });
    }

    @Loggable
    @Override
    public CRDTRequest generateCrdtRequest() {
        return buildCrdtRequest(Instant.MIN);
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

    private void replicate(Instant startingFromEpoch) {
        var data = buildCrdtRequest(startingFromEpoch);
        var request = Request.buildRequest(Command.SYNCHRONIZE, data);
        utils.filterNodesByState(internalCache.getAll(), uniqueId.number(), List.of(RUNNING))
                .forEach(
                        node ->
                                client.requestAsync(request, node.ip(), node.port())
                                        .handle(
                                                (response, error) -> {
                                                    if (error != null) {
                                                        logger.error("Request failed", error);
                                                        internalCache.put(
                                                                node.uniqueId().toString(),
                                                                node.clone(FAILING));
                                                    }
                                                    return response;
                                                }));
    }

    private Entry buildEntry(String key, Object value) {
        return new Entry(Instant.now(), key, value);
    }

    private CRDTRequest buildCrdtRequest(Instant startingFromEpoch) {
        var add =
                state.getAdd().stream()
                        .filter(e -> !e.timestamp().isBefore(startingFromEpoch))
                        .toList();
        var remove =
                state.getRemove().stream()
                        .filter(e -> !e.timestamp().isBefore(startingFromEpoch))
                        .toList();

        return new CRDTRequest(add, remove);
    }
}
