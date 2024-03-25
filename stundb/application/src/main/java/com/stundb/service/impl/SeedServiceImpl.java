package com.stundb.service.impl;

import static java.lang.String.format;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.RegisterResponse;
import com.stundb.service.ReplicationService;
import com.stundb.service.SeedService;
import com.stundb.timers.BackoffTimerTask;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class SeedServiceImpl implements SeedService {

    @Inject protected StunDBClient client;
    @Inject private ApplicationConfig config;
    @Inject private Cache<Node> internalCache;
    @Inject private ReplicationService replicationService;
    @Inject private UniqueId uniqueId;
    @Inject private BackoffTimerTask backoffTimerTask;
    @Inject private Timer timer;

    @Override
    @Loggable
    public void contactSeeds() {
        var otherSeeds =
                config.seeds().stream()
                        .filter(seed -> !seed.equals(config.ip() + ":" + config.port()))
                        .toList();

        otherSeeds.forEach(seed -> backoffTimerTask.enqueue(seed, this::contactSeed));
        timer.schedule((TimerTask) backoffTimerTask, 1000L);
    }

    @Loggable
    private Void contactSeed(String seed) {
        var address = seed.split(":");
        if (address.length != 2) {
            throw new IllegalArgumentException(format("Invalid seed address - %s", seed));
        }

        var request =
                Request.buildRequest(
                        Command.REGISTER,
                        new RegisterRequest(config.ip(), config.port(), uniqueId.number()));
        var response =
                client.requestAsync(request, address[0], Integer.parseInt(address[1])).join();
        if (Status.ERROR.equals(response.status())) {
            var code = ((ErrorResponse) response.payload()).code();
            throw new RuntimeException(format("Reply from seed was - %s", code));
        }
        handleSeedResponse((RegisterResponse) response.payload());
        return null;
    }

    @Loggable
    private void handleSeedResponse(RegisterResponse response) {
        response.nodes().forEach(entry -> internalCache.upsert(entry.uniqueId().toString(), entry));
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
        replicationService.synchronize(response.state().added(), response.state().removed());
    }
}
