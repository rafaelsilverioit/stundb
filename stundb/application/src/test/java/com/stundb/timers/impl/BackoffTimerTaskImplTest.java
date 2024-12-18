package com.stundb.timers.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Executor;
import com.stundb.api.models.Executors;
import com.stundb.api.models.Tuple;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class BackoffTimerTaskImplTest {

    private static final Function<String, Void> DEFAULT_TASK = __ -> null;
    private static final Function<String, Void> TASK_THAT_FAILS =
            __ -> {
                throw new RuntimeException();
            };
    private static final String JON_DOE = "jon doe";
    private static final String ATTEMPT = "attempt";
    private static final String SCHEDULED_TASK = "scheduledTask";
    private static final String TASKS = "tasks";
    private static final String MAXIMUM_RETRIES = "maximumRetries";
    private static final String MAXIMUM_BACKOFF_IN_SECONDS = "maximumBackoffInSeconds";
    private final TestLogger logger = TestLoggerFactory.getTestLogger(BackoffTimerTaskImpl.class);
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
        testee = new BackoffTimerTaskImpl(config);
    }

    @AfterEach
    void tearDown() {
        logger.clear();
    }

    @Test
    void enqueue() throws NoSuchFieldException, IllegalAccessException {
        Queue<Tuple<String, Function<String, Void>>> tasks = getValue(TASKS);

        testee.enqueue(JON_DOE, DEFAULT_TASK);

        assertEquals(1, tasks.size());
    }

    @Test
    void run_should_execute_successfully() throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getValue(ATTEMPT);
        ScheduledFuture<?> scheduledTask = getValue(SCHEDULED_TASK);

        testee.enqueue(JON_DOE, DEFAULT_TASK);
        testee.run();

        assertEquals(1, attempt.get());
        assertNull(scheduledTask);
    }

    @Test
    void run_should_schedule_to_retry_upon_an_exception()
            throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getValue(ATTEMPT);

        testee.enqueue(JON_DOE, TASK_THAT_FAILS);
        testee.run();

        ScheduledFuture<?> scheduledTask = getValue(SCHEDULED_TASK);

        assertThat(logger.getLoggingEvents(), hasSize(2));
        assertEquals(1, attempt.get());
        assertNotNull(scheduledTask);
        assertFalse(scheduledTask.isCancelled());
    }

    @Test
    void run_should_cancel_further_retries_when_number_of_attempts_exceed_maximumRetries()
            throws NoSuchFieldException, IllegalAccessException {
        AtomicInteger attempt = getValue(ATTEMPT);

        testee.enqueue(JON_DOE, TASK_THAT_FAILS);

        // run to create scheduled task instance
        testee.run();
        // set attempts to the same value as maximumRetries
        attempt.set(3);
        // then cancels scheduled task
        testee.run();

        ScheduledFuture<?> scheduledTask = getValue(SCHEDULED_TASK);
        assertNotNull(scheduledTask);
        assertTrue(scheduledTask.isCancelled());
    }

    private <T> T getValue(String fieldName) throws IllegalAccessException, NoSuchFieldException {
        var field = getField(fieldName);
        //noinspection unchecked
        return (T) field.get(testee);
    }

    private Field getField(String fieldName) throws NoSuchFieldException {
        var field = BackoffTimerTaskImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
