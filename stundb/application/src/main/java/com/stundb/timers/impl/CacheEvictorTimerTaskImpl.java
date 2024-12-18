package com.stundb.timers.impl;

import com.stundb.core.cache.Cache;
import com.stundb.net.core.models.requests.DelRequest;
import com.stundb.service.StoreService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Singleton
public class CacheEvictorTimerTaskImpl extends TimerTask {

    @Inject private Cache<Object> cache;
    @Inject private StoreService storeService;

    @Override
    public void run() {
        cache.retrieveKeysOfExpiredEntries()
                .forEach(
                        key -> {
                            log.info("Removing expired key: {}", key);
                            storeService.del(new DelRequest(key));
                        });
    }
}
