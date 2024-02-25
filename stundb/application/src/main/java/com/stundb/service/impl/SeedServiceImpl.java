package com.stundb.service.impl;

import static java.lang.String.format;

import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.models.Node;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.RegisterResponse;
import com.stundb.service.ReplicationService;
import com.stundb.service.SeedService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.SneakyThrows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class SeedServiceImpl implements SeedService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject protected StunDBClient client;
    @Inject private ApplicationConfig config;
    @Inject private Cache<Node> internalCache;
    @Inject private ReplicationService replicationService;
    @Inject private UniqueId uniqueId;

    @Override
    @Loggable
    @SneakyThrows
    public void contactSeeds() {
        var otherSeeds =
                config.getSeeds().stream()
                        .filter(seed -> !seed.equals(config.getIp() + ":" + config.getPort()))
                        .toList();
        var hasAnySeedReplied = new AtomicBoolean(false);
        var attempt = 0;

        // retrying to ping seeds until at least one node replies back
        while (!hasAnySeedReplied.get()) {
            if (attempt >= readConfigAsInteger("maximumRetries", 5)) {
                logger.warn("No seeds replied back after many attempts, giving up");
                break;
            } else if (attempt != 0) {
                var sleep = calculateBackoffTimeInMillis(attempt);
                logger.warn(
                        "All seeds have failed, waiting for {} milliseconds before retrying",
                        sleep);
                //noinspection BusyWait
                Thread.sleep(sleep);
            }

            otherSeeds.forEach(seed -> contactSeed(seed, hasAnySeedReplied));
            attempt++;
        }
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
        var upperbound = readConfigAsInteger("maximumBackoffInSeconds", 60);
        var backoff = attempt + new Random().nextInt(1, upperbound);
        var backoffInSeconds = Math.min(upperbound, backoff);
        return backoffInSeconds * 1000L;
    }

    private void contactSeed(String seed, AtomicBoolean hasAnySeedReplied) {
        try {
            contactSeed(seed);
            hasAnySeedReplied.set(true);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
    }

    @Loggable
    private void contactSeed(String seed) throws ExecutionException, InterruptedException {
        var address = seed.split(":");
        if (address.length != 2) {
            throw new IllegalArgumentException(format("Invalid seed address - %s", seed));
        }

        var request =
                Request.buildRequest(
                        Command.REGISTER,
                        new RegisterRequest(config.getIp(), config.getPort(), uniqueId.number()));
        var response = client.requestAsync(request, address[0], Integer.parseInt(address[1])).get();
        if (Status.ERROR.equals(response.status())) {
            var code = ((ErrorResponse) response.payload()).code();
            throw new RuntimeException(format("Reply from seed was - %s", code));
        }
        handleSeedResponse((RegisterResponse) response.payload());
    }

    @Loggable
    private void handleSeedResponse(RegisterResponse response) {
        response.nodes().forEach(entry -> internalCache.put(entry.uniqueId().toString(), entry));
        /*
         * TODO: Perhaps we should change how synchronization works - meaning:
         *  1- Should we rely on seeds telling us what data has been processed so far?
         *  2- Or should we ask the cluster leader for it?
         *  3- Or even, ask any node at random?
         *  I guess options #2 and #3 have the advantage of we being able to ask for synchronization
         *  at any time, however, option #2 may be overflow the leader with too many requests and
         *  network traffic... option #3 may not be a good options because we may end up choosing
         *  a node that just became part of the cluster not having been synchronized yet... Option #1
         *  is good, however, means we can't ask for a synchronization at our will... moreover,
         *  what if all seeds go down?
         *  We can possibly mix all three options, we start by registering with seed nodes, they then
         *  reply with sync data, then, later on, we may ask them for another synchronization, if
         *  they aren't available, we can then ask the current cluster leader (unless we are the
         *  current leader, in which case we can consider we are up to date with the rest of the
         *  cluster), if the current leader isn't available, we can then fallback to asking any node
         *  at random.
         */
        replicationService.synchronize(response.state());
    }

    private Integer readConfigAsInteger(String key, int defaultValue) {
        return (Integer) config.getBackoffSettings().getOrDefault(key, defaultValue);
    }
}
