package com.stundb.timers.impl;

import static org.mockito.Mockito.*;

import com.stundb.core.cache.Cache;
import com.stundb.service.StoreService;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class CacheEvictorTimerTaskImplTest {

    @Mock private Cache<Object> cache;
    @Mock private StoreService storeService;

    @InjectMocks private CacheEvictorTimerTaskImpl testee;

    private static Stream<Arguments> run_arguments() {
        return Stream.of(Arguments.of(List.of()), Arguments.of(List.of("key")));
    }

    @ParameterizedTest
    @MethodSource("run_arguments")
    void run(List<String> expiredKeys) {
        when(cache.retrieveKeysOfExpiredEntries()).thenReturn(expiredKeys);

        testee.run();

        verify(storeService, times(expiredKeys.size())).del(ArgumentMatchers.any());
    }
}
