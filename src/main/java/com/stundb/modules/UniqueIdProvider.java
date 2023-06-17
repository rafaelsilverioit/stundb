package com.stundb.modules;

import com.stundb.models.ApplicationConfig;
import com.stundb.models.UniqueId;
import com.stundb.utils.NodeUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
class UniqueIdProvider implements Provider<UniqueId> {

    @Inject
    private ApplicationConfig config;

    @Inject
    private NodeUtils utils;

    @Override
    public UniqueId get() {
        long uniqueId = utils.generateUniqueId(config.getIp() + ":" + config.getPort());
        return UniqueId.builder()
                .number(uniqueId)
                .text(String.valueOf(uniqueId))
                .build();
    }
}
