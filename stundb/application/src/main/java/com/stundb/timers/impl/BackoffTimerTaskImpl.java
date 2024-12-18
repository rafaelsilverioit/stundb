package com.stundb.timers.impl;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Tuple;
import com.stundb.core.logging.Loggable;
import com.stundb.timers.BackoffTimerTask;

import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class BackoffTimerTaskImpl extends TimerTask implements BackoffTimerTask {

    private final AtomicInteger attempt = new AtomicInteger(0);
    private final Integer maximumRetries;
    private final ScheduledExecutorService scheduler;
    private final ApplicationConfig config;
    private final Queue<Tuple<String, Function<String, Void>>> tasks = new LinkedList<>();
    private final Integer maximumBackoffInSeconds;
    private ScheduledFuture<?> scheduledTask;

    @Inject
    public BackoffTimerTaskImpl(ApplicationConfig config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(config.executors().scheduler().threads());
        this.maximumRetries = readConfigAsInteger("maximumRetries", 5);
        this.maximumBackoffInSeconds = readConfigAsInteger("maximumBackoffInSeconds", 60);
    }

    @Loggable
    @Override
    public void run() {
        var reschedule = process();
        var nextAttempt = attempt.incrementAndGet();

        if (!reschedule || nextAttempt >= maximumRetries) {
            cancel(reschedule, nextAttempt);
            return;
        }

        reschedule(nextAttempt);
    }

    @Override
    public void enqueue(String identifier, Function<String, Void> task) {
        tasks.add(new Tuple<>(identifier, task));
    }

    private boolean process() {
        var reschedule = false;

        for (var task : tasks) {
            var identifier = task.left();
            try {
                task.right().apply(identifier);
                log.info("Task {} has succeeded, stopping further retries", identifier);
                return false;
            } catch (Exception e) {
                log.warn("Task {} failed, rescheduling", identifier, e);
                reschedule = true;
            }
        }

        return reschedule;
    }

    /**
     *
     *
     * <pre>
     * Given an attempt number, calculates the backoff time with some random jitter.
     *
     * Formula: <b>min(maximumBackoffInSeconds, attempt + random jitter)</b>
     * Given that <b>random jitter</b> is a random number between <b>1</b> and <b>maximumBackoffInSeconds</b> (exclusive).
     * </pre>
     *
     * @param attempt the number of failed requests
     * @return backoff in milliseconds
     */
    private long calculateBackoffTimeInMillis(int attempt) {
        var backoff = attempt + new Random().nextInt(1, maximumBackoffInSeconds);
        var backoffInSeconds = Math.min(maximumBackoffInSeconds, backoff);
        return backoffInSeconds * 1000L;
    }

    private Integer readConfigAsInteger(String key, int defaultValue) {
        return config.backoffSettings().getOrDefault(key, defaultValue);
    }

    private void cancel(boolean reschedule, int nextAttempt) {
        log.info(
                "Cancelling further retries. Has task succeeded? {}, Maximum retries reached? {}",
                !reschedule,
                nextAttempt >= maximumRetries);

        ofNullable(scheduledTask)
                .filter(not(ScheduledFuture::isCancelled))
                .ifPresent(task -> task.cancel(true));
    }

    private void reschedule(int nextAttempt) {
        var sleep = calculateBackoffTimeInMillis(nextAttempt);
        log.warn("Failed to execute task, waiting for {} milliseconds before retrying", sleep);
        scheduledTask = scheduler.schedule(this, sleep, TimeUnit.MILLISECONDS);
    }
}
