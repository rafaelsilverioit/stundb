package com.stundb.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stundb.api.crdt.Entry;
import com.stundb.core.crdt.CRDT;

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
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ReplicationServiceImplTest {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private CRDT state;

    @InjectMocks private ReplicationServiceImpl testee;

    private static Stream<Arguments>
            verifySynchroneity_should_return_nothing_when_provided_clock_has_a_greater_value_or_value_is_null_arguments() {
        return Stream.of(Arguments.of(5L), Arguments.of((Long) null));
    }

    @Test
    void generateVersionClock() {
        var versionClock = Map.of(KEY, 1L);
        when(state.versionClock()).thenReturn(versionClock);

        var data = testee.generateVersionClock();

        verify(state, times(1)).versionClock();
        assertEquals(versionClock, data);
    }

    @Test
    void generateStateSnapshot_should_return_all_changes() {
        var entry = new Entry(Instant.MIN, KEY, VALUE);
        var updatedEntry = new Entry(Instant.MAX, KEY, VALUE);

        when(state.getAdded()).thenReturn(Set.of(entry));
        when(state.getRemoved()).thenReturn(Set.of(updatedEntry));

        var data = testee.generateStateSnapshot();

        verify(state, times(1)).getAdded();
        verify(state, times(1)).getRemoved();

        assertEquals(List.of(entry), data.left());
        assertEquals(List.of(updatedEntry), data.right());
    }

    @Test
    void generateStateSnapshot_should_return_nothing_when_no_changes_have_been_recorded() {
        when(state.getAdded()).thenReturn(Set.of());
        when(state.getRemoved()).thenReturn(Set.of());

        var data = testee.generateStateSnapshot();

        verify(state, times(1)).getAdded();
        verify(state, times(1)).getRemoved();

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

        verify(state, times(1)).versionClock();
        verify(state, times(1)).getAdded();
        verify(state, times(1)).getRemoved();

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

        verify(state, times(1)).versionClock();
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

        verify(state, times(1)).versionClock();
        verify(state, times(1)).getAdded();
        verify(state, times(1)).getRemoved();

        assertEquals(0, data.left().size());
        assertEquals(0, data.right().size());
    }
}
