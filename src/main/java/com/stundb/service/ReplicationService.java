package com.stundb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stundb.cache.Cache;
import com.stundb.clients.GrpcRunner;
import com.stundb.crdt.Entry;
import com.stundb.crdt.LastWriterWinsSet;
import com.stundb.logging.Loggable;
import com.stundb.models.UniqueId;
import com.stundb.observers.NoOpObserver;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

@Singleton
public class ReplicationService implements SyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LastWriterWinsSet state = new LastWriterWinsSet();

    @Inject
    private GrpcRunner<CRDTRequest, CRDTResponse> runner;

    @Inject
    private Cache<String> cache;

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private UniqueId uniqueId;

    @Inject
    private ObjectMapper mapper;

    @Loggable
    public void synchronize(CRDTRequest request) {
        var lww = new LastWriterWinsSet();
        var add = request.getAddSetList()
                .stream()
                .map(this::deserialize)
                .toList();
        var remove = request.getRemoveSetList()
                .stream()
                .map(this::deserialize)
                .toList();

        lww.getAdd().addAll(add);
        lww.getRemove().addAll(remove);

        state.merge(lww);

        var added = state.getAdd();
        var removed = state.getRemove();

        add.stream()
                .filter(e ->
                        removed.stream().noneMatch(e2 -> e2.key().equals(e.key()) && e2.timestamp() > e.timestamp()))
                .forEach(e -> cache.put(e.key(), e.value()));

        remove.stream()
                .filter(e ->
                        added.stream().noneMatch(e2 -> e2.key().equals(e.key()) && e2.timestamp() >= e.timestamp()))
                .forEach(e -> {
                    cache.del(e.key());
                    added.stream()
                            .filter(entry -> e.key().equals(entry.key()) && e.timestamp() > entry.timestamp())
                            .forEach(entry -> {
                                added.remove(entry);
                                added.add(entry.toBuilder().value(null).build());
                            });
                });
    }

    @Loggable
    @SneakyThrows
    public CRDTRequest generateCrdtRequest() {
        logger.info(mapper.writeValueAsString(state));
        return buildCrdtRequest(-1);
    }

    public void add(String key, String value) {
        var entry = buildEntry(key, value);
        state.add(entry);
        replicate(entry.timestamp());
    }

    public void remove(String key) {
        var entry = buildEntry(key, null);
        state.remove(entry);
        replicate(entry.timestamp());
    }

    private void replicate(long startingFromEpoch) {
        var request = buildCrdtRequest(startingFromEpoch);
        internalCache.getAll()
                .stream()
                .filter(node -> node.getUniqueId() != uniqueId.getNumber())
                .forEach(node -> runner.run(node.getIp(), node.getPort(), request, new NoOpObserver<>()));
    }

    private Entry buildEntry(String key, String value) {
        return Entry.builder()
                .timestamp(Instant.now().toEpochMilli())
                .key(key)
                .value(value)
                .build();
    }

    private CRDTRequest buildCrdtRequest(long startingFromEpoch) {
        var add = state.getAdd()
                .stream()
                .filter(e -> e.timestamp() >= startingFromEpoch)
                .map(this::serialize)
                .toList();
        var remove = state.getRemove()
                .stream()
                .filter(e -> e.timestamp() >= startingFromEpoch)
                .map(this::serialize)
                .toList();

        return CRDTRequest.newBuilder()
                .addAllAddSet(add)
                .addAllRemoveSet(remove)
                .build();
    }

    @SneakyThrows
    private String serialize(Entry entry) {
        return mapper.writeValueAsString(entry);
    }

    @SneakyThrows
    private Entry deserialize(String bytes) {
        return mapper.readValue(bytes, Entry.class);
    }
}
