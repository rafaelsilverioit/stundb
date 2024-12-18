package com.stundb.server;

import com.stundb.net.server.TcpServer;
import com.stundb.service.NodeService;
import com.stundb.service.SeedService;
import com.stundb.service.StoreService;

import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpServerImpl extends TcpServer {

    @Inject private NodeService nodeService;
    @Inject private StoreService storeService;
    @Inject private SeedService seedService;

    @Override
    protected void onStart() {
        nodeService.init();
        storeService.init();
        log.info("Running {} on {}", config.name(), serverAddress());
    }

    @Override
    protected void contactSeeds() {
        seedService.contactSeeds();
    }
}
