package com.stundb.service.impl;

import com.stundb.annotations.CacheEvictor;
import com.stundb.core.cache.Cache;
import com.stundb.core.logging.Loggable;
import com.stundb.net.core.models.requests.DelRequest;
import com.stundb.net.core.models.requests.ExistsRequest;
import com.stundb.net.core.models.requests.GetRequest;
import com.stundb.net.core.models.requests.SetRequest;
import com.stundb.net.core.models.responses.CapacityResponse;
import com.stundb.net.core.models.responses.ExistsResponse;
import com.stundb.net.core.models.responses.GetResponse;
import com.stundb.net.core.models.responses.IsEmptyResponse;
import com.stundb.service.StoreService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class StoreServiceImpl implements StoreService {

    @Inject private Cache<Object> cache;
    @Inject private ReplicationServiceImpl replicationService;
    @Inject private Timer timer;

    @Inject @CacheEvictor private TimerTask cacheEvictorTimerTask;

    @Override
    public void init() {
        timer.scheduleAtFixedRate(cacheEvictorTimerTask, 10, 15 * 1000);
    }

    @Loggable
    public void set(SetRequest request) {
        var encoded = request.value();
        cache.get(request.key()).ifPresent(__ -> replicationService.remove(request.key()));
        cache.upsert(request.key(), encoded, request.ttl());
        replicationService.add(request.key(), encoded);
    }

    @Loggable
    public void del(DelRequest request) {
        cache.del(request.key());
        replicationService.remove(request.key());
    }

    @Loggable
    public GetResponse get(GetRequest request) {
        var data = cache.get(request.key()).orElse(null);
        return new GetResponse(request.key(), data);
    }

    @Loggable
    public IsEmptyResponse isEmpty() {
        return new IsEmptyResponse(cache.isEmpty());
    }

    @Loggable
    public CapacityResponse capacity() {
        return new CapacityResponse(cache.capacity());
    }

    @Loggable
    public void clear() {
        cache.clear();
    }

    @Loggable
    public ExistsResponse exists(ExistsRequest request) {
        return new ExistsResponse(cache.get(request.key()).isPresent());
    }
}
