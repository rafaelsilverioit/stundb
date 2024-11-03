package com.stundb.timers.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Executor;
import com.stundb.api.models.Executors;
import com.stundb.api.models.Tuple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class BackoffTimerTaskImplTest {

    private static final Function<String, Void> DEFAULT_TASK = __ -> null;
    private static final Function<String, Void> TASK_THAT_FAILS = __ -> {
        throw new RuntimeException();
    };
    private static final String JON_DOE = "jon doe";
    private static final String ATTEMPT = "attempt";
    private static final String SCHEDULED_TASK = "scheduledTask";
    private static final String TASKS = "tasks";
    private static final String MAXIMUM_RETRIES = "maximumRetries";
    private static final String MAXIMUM_BACKOFF_IN_SECONDS = "maximumBackoffInSeconds";
    @Mock private Timer timer;
    @Mock private ApplicationConfig config;

    private BackoffTimerTaskImpl testee;

    @BeforeEach
    void setUp() {
        when(config.backoffSettings())
                .thenReturn(
                        Map.of(
                                MAXIMUM_RETRIES, 3,
                                MAXIMUM_BACKOFF_IN_SECONDS, 10));
        when(config.executors())
                .thenReturn(
                        new Executors(
                                new Executor(1),
                                new Executor(1),
                                new Executor(1),
                                new Executor(1),
                                new Executor(1)));
        testee = new BackoffTimerTaskImpl(timer, config);
    }

    @Test
    void enqueue() throws NoSuchFieldException, IllegalAccessException {
        Queue<Tuple<String, Function<String, Void>>> tasks = getField(TASKS);

        testee.enqueue(JON_DOE, DEFAULT_TASK);

        assertEquals(1, tasks.size());
    }

    @Test
    void run_should_execute_successfully() throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getField(ATTEMPT);
        ScheduledFuture<?> scheduledTask = getField(SCHEDULED_TASK);

        testee.enqueue(JON_DOE, DEFAULT_TASK);
        testee.run();

        verify(timer, times(1)).cancel();
        verify(timer, times(1)).purge();

        assertEquals(1, attempt.get());
        assertNull(scheduledTask);
    }

    @Test
    void run_should_schedule_to_retry_upon_an_exception()
            throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getField(ATTEMPT);

        testee.enqueue(JON_DOE, TASK_THAT_FAILS);
        testee.run();

        ScheduledFuture<?> scheduledTask = getField(SCHEDULED_TASK);

        verify(timer, never()).cancel();
        verify(timer, never()).purge();

        assertEquals(1, attempt.get());
        assertNotNull(scheduledTask);
        assertFalse(scheduledTask.isCancelled());
    }

    @Test
    void run_should_cancel_further_retries_when_number_of_attempts_exceed_maximumRetries()
            throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getField(ATTEMPT);

        testee.enqueue(JON_DOE, TASK_THAT_FAILS);

        // run to create scheduled task instance
        testee.run();
        // set attempts to the same value as maximumRetries
        attempt.set(3);
        // then cancels scheduled task
        testee.run();

        ScheduledFuture<?> scheduledTask = getField(SCHEDULED_TASK);

        verify(timer, times(1)).cancel();
        verify(timer, times(1)).purge();

        assertNotNull(scheduledTask);
        assertTrue(scheduledTask.isCancelled());
    }

    private <T> T getField(String fieldName) throws IllegalAccessException, NoSuchFieldException {
        var field = BackoffTimerTaskImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        //noinspection unchecked
        return (T) field.get(testee);
    }
}
