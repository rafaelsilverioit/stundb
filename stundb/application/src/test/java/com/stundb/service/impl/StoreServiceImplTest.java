package com.stundb.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stundb.core.cache.Cache;
import com.stundb.net.core.models.requests.DelRequest;
import com.stundb.net.core.models.requests.ExistsRequest;
import com.stundb.net.core.models.requests.GetRequest;
import com.stundb.net.core.models.requests.SetRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreServiceImplTest {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private Cache<Object> cache;
    @Mock private ReplicationServiceImpl replicationService;
    @Mock private TimerTask timerTask;
    @Mock private Timer timer;
    @InjectMocks private StoreServiceImpl testee;

    private Stream<Arguments> existsArguments() {
        return Stream.of(
                Arguments.of(Optional.empty(), false), Arguments.of(Optional.of(VALUE), true));
    }

    @Test
    void init_should_work_successfully() {
        testee.init();

        verify(timer).scheduleAtFixedRate(eq(timerTask), anyLong(), anyLong());
    }

    @Test
    void set_should_store_data_successfully() {
        when(cache.upsert(KEY, VALUE, -1L)).thenReturn(true);

        testee.set(new SetRequest(KEY, VALUE, -1L));

        verify(cache).upsert(KEY, VALUE, -1L);
        verify(replicationService, never()).remove(KEY);
        verify(replicationService).add(KEY, VALUE);
    }

    @Test
    void set_should_store_data_successfully_and_handle_duplicates() {
        SetRequest request = new SetRequest(KEY, VALUE, -1L);

        when(cache.upsert(KEY, VALUE, -1L)).thenReturn(true);
        when(cache.get(KEY)).thenReturn(Optional.empty()).thenReturn(Optional.of(VALUE));

        testee.set(request);
        testee.set(request);

        verify(cache, times(2)).upsert(KEY, VALUE, -1L);
        verify(replicationService).remove(KEY);
        verify(replicationService, times(2)).add(KEY, VALUE);
    }

    @Test
    void del_should_remove_data_successfully() {
        when(cache.del(KEY)).thenReturn(true);

        testee.del(new DelRequest(KEY));

        verify(cache).del(KEY);
        verify(replicationService).remove(KEY);
    }

    @Test
    void get_should_retrieve_data_successfully_when_value_is_present() {
        when(cache.get(KEY)).thenReturn(Optional.of(VALUE));

        var response = testee.get(new GetRequest(KEY));

        verify(cache).get(KEY);

        assertEquals(KEY, response.key());
        assertEquals(VALUE, response.value());
    }

    @Test
    void get_should_retrieve_data_successfully_when_value_is_absent() {
        when(cache.get(KEY)).thenReturn(Optional.empty());

        var response = testee.get(new GetRequest(KEY));

        verify(cache).get(KEY);

        assertEquals(KEY, response.key());
        assertNull(response.value());
    }

    @ParameterizedTest
    @MethodSource("existsArguments")
    void exists_should_tell_if_a_given_key_is_stored_in_the_cache(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Object> value,
            boolean expected) {
        when(cache.get(KEY)).thenReturn(value);

        var response = testee.exists(new ExistsRequest(KEY));

        verify(cache).get(KEY);

        assertEquals(expected, response.exists());
    }

    @Test
    void isEmpty_should_return_whether_cache_is_empty_or_not() {
        when(cache.isEmpty()).thenReturn(true);

        assertTrue(testee.isEmpty().isEmpty());
    }

    @Test
    void capacity_should_return_the_total_capacity() {
        when(cache.capacity()).thenReturn(10);

        assertEquals(10, testee.capacity().capacity());
    }

    @Test
    void clear_should_empty_the_cache() {
        testee.clear();

        verify(cache).clear();
    }
}
