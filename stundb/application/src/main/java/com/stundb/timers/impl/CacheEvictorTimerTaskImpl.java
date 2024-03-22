package com.stundb.timers.impl;

import com.stundb.core.cache.Cache;
import com.stundb.net.core.models.requests.DelRequest;
import com.stundb.service.StoreService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class CacheEvictorTimerTaskImpl extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private Cache<Object> cache;
    @Inject private StoreService storeService;

    @Override
    public void run() {
        cache.retrieveKeysOfExpiredEntries()
                .forEach(
                        key -> {
                            logger.info("Removing expired key: {}", key);
                            storeService.del(new DelRequest(key));
                        });
    }
}
