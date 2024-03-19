package com.stundb.modules.providers;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.models.UniqueId;
import com.stundb.utils.NodeUtils;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class UniqueIdProvider implements Provider<UniqueId> {

    @Inject private ApplicationConfig config;
    @Inject private NodeUtils utils;

    @Override
    public UniqueId get() {
        long uniqueId = utils.generateUniqueId(config.ip() + ":" + config.port());
        return new UniqueId(String.valueOf(uniqueId), uniqueId);
    }
}
