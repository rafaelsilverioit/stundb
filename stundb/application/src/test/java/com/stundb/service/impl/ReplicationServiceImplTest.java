package com.stundb.service.impl;

import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stundb.api.crdt.Entry;
import com.stundb.core.cache.Cache;
import com.stundb.core.crdt.CRDT;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.responses.Response;
import com.stundb.utils.NodeUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ReplicationServiceImplTest {

    private static final String KEY = "key";
    private static final String ANOTHER_KEY = "key2";
    private static final String VALUE = "value";

    private static final Node NODE =
            new Node("127.0.0.1", 9000, 1L, true, NodeStatus.create(RUNNING));

    @Mock private CRDT state;
    @Mock private StunDBClient client;
    @Mock private Cache<Object> cache;
    @Mock private Cache<Node> internalCache;
    @Mock private UniqueId uniqueId;
    @Mock private NodeUtils utils;

    @InjectMocks private ReplicationServiceImpl testee;

    private static Stream<Arguments>
            verifySynchroneity_should_return_nothing_when_provided_clock_has_a_greater_value_or_value_is_null_arguments() {
        return Stream.of(Arguments.of(5L), Arguments.of((Long) null));
    }

    private static Stream<Arguments> add_or_remove_arguments() {
        return Stream.of(
                Arguments.of(
                        CompletableFuture.completedFuture(
                                new Response(null, null, null, null, null)),
                        0),
                Arguments.of(CompletableFuture.failedFuture(new RuntimeException()), 1));
    }

    private static Stream<Arguments> synchronize_arguments() {
        return Stream.of(
                Arguments.of(Instant.MAX, Instant.MIN, ANOTHER_KEY, 1, 1),
                Arguments.of(Instant.MAX, Instant.MAX, KEY, 1, 0),
                Arguments.of(Instant.MIN, Instant.MAX, KEY, 0, 1));
    }

    @Test
    void generateVersionClock() {
        var versionClock = Map.of(KEY, 1L);
        when(state.versionClock()).thenReturn(versionClock);

        var data = testee.generateVersionClock();

        verify(state).versionClock();
        assertEquals(versionClock, data);
    }

    @Test
    void generateStateSnapshot_should_return_all_changes() {
        var entry = new Entry(Instant.MIN, KEY, VALUE);
        var updatedEntry = new Entry(Instant.MAX, KEY, VALUE);

        when(state.getAdded()).thenReturn(Set.of(entry));
        when(state.getRemoved()).thenReturn(Set.of(updatedEntry));

        var data = testee.generateStateSnapshot();

        verify(state).getAdded();
        verify(state).getRemoved();

        assertEquals(List.of(entry), data.left());
        assertEquals(List.of(updatedEntry), data.right());
    }

    @Test
    void generateStateSnapshot_should_return_nothing_when_no_changes_have_been_recorded() {
        when(state.getAdded()).thenReturn(Set.of());
        when(state.getRemoved()).thenReturn(Set.of());

        var data = testee.generateStateSnapshot();

        verify(state).getAdded();
        verify(state).getRemoved();

        assertEquals(List.of(), data.left());
        assertEquals(List.of(), data.right());
    }

    @Test
    void
            verifySynchroneity_should_return_delta_between_internal_state_and_the_provided_version_clock() {
        var entry = new Entry(Instant.MAX, KEY, VALUE);
        var versionClock = Map.of(KEY, 3L);
        var theirClock = Map.of(KEY, 1L);

        when(state.versionClock()).thenReturn(versionClock);
        when(state.getAdded()).thenReturn(Set.of(entry));
        when(state.getRemoved()).thenReturn(Set.of());

        var data = testee.verifySynchroneity(theirClock);

        verify(state).versionClock();
        verify(state).getAdded();
        verify(state).getRemoved();

        var addedEntry = ((List<Entry>) data.left()).get(0);
        assertEquals(KEY, addedEntry.key());
        assertEquals(VALUE, addedEntry.value());
        assertEquals(Instant.MAX, addedEntry.timestamp());
    }

    @Test
    void verifySynchroneity_should_return_nothing_when_both_clocks_are_equal() {
        var versionClock = Map.of(KEY, 3L);
        var theirClock = Map.of(KEY, 3L);

        when(state.versionClock()).thenReturn(versionClock);

        var data = testee.verifySynchroneity(theirClock);

        verify(state).versionClock();
        verify(state, never()).getAdded();
        verify(state, never()).getRemoved();

        assertEquals(0, data.left().size());
        assertEquals(0, data.right().size());
    }

    @ParameterizedTest
    @MethodSource(
            "verifySynchroneity_should_return_nothing_when_provided_clock_has_a_greater_value_or_value_is_null_arguments")
    void
            verifySynchroneity_should_return_nothing_when_provided_clock_has_a_greater_value_or_value_is_null(
                    Long value) {
        var versionClock = Map.of(KEY, 3L);
        var theirClock = new HashMap<String, Long>();
        theirClock.put(KEY, value);

        when(state.versionClock()).thenReturn(versionClock);

        var data = testee.verifySynchroneity(theirClock);

        verify(state).versionClock();
        verify(state).getAdded();
        verify(state).getRemoved();

        assertEquals(0, data.left().size());
        assertEquals(0, data.right().size());
    }

    @ParameterizedTest
    @MethodSource("synchronize_arguments")
    void synchronize(
            Instant addedTimestamp,
            Instant removedTimestamp,
            String removedKey,
            Integer expectedUpsertCalls,
            Integer expectedDelCalls) {
        var entry = new Entry(addedTimestamp, KEY, VALUE);
        var removedEntry = new Entry(removedTimestamp, removedKey, VALUE);

        when(state.getAdded()).thenReturn(Set.of(entry));
        when(state.getRemoved()).thenReturn(Set.of(removedEntry));

        testee.synchronize(List.of(entry), List.of(removedEntry));

        verify(state).merge(anyCollection(), anyCollection());
        verify(state).getRemoved();
        verify(state).getAdded();
        verify(cache, times(expectedUpsertCalls)).upsert(anyString(), any());
        verify(cache, times(expectedDelCalls)).del(anyString());
    }

    @ParameterizedTest
    @MethodSource("add_or_remove_arguments")
    void add(CompletableFuture<Response> future, Integer expectedUpsertCalls) {
        var removedEntry = new Entry(Instant.MIN, ANOTHER_KEY, VALUE);

        when(state.getAdded()).thenReturn(Set.of());
        when(state.getRemoved()).thenReturn(Set.of(removedEntry));
        when(utils.filterNodesByState(anyCollection(), anyLong(), anyList()))
                .thenReturn(Stream.of(NODE));
        when(client.requestAsync(any(), anyString(), anyInt())).thenReturn(future);

        testee.add(KEY, VALUE);

        verify(state).add(any());
        verify(utils).filterNodesByState(anyCollection(), anyLong(), anyList());
        verify(internalCache).getAll();
        verify(uniqueId).number();
        verify(client).requestAsync(any(), anyString(), anyInt());
        verify(internalCache, times(expectedUpsertCalls)).upsert(anyString(), any());
    }

    @ParameterizedTest
    @MethodSource("add_or_remove_arguments")
    void remove(CompletableFuture<Response> future, Integer expectedUpsertCalls) {
        var removedEntry = new Entry(Instant.MAX, KEY, VALUE);

        when(state.getAdded()).thenReturn(Set.of());
        when(state.getRemoved()).thenReturn(Set.of(removedEntry));
        when(utils.filterNodesByState(anyCollection(), anyLong(), anyList()))
                .thenReturn(Stream.of(NODE));
        when(client.requestAsync(any(), anyString(), anyInt())).thenReturn(future);

        testee.remove(KEY);

        verify(state).remove(any());
        verify(utils).filterNodesByState(anyCollection(), anyLong(), anyList());
        verify(internalCache).getAll();
        verify(uniqueId).number();
        verify(client).requestAsync(any(), anyString(), anyInt());
        verify(internalCache, times(expectedUpsertCalls)).upsert(anyString(), any());
    }
}
