package com.stundb.server;

import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.server.TcpServer;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import com.stundb.service.SeedService;
import com.stundb.service.StoreService;

import jakarta.inject.Inject;

public class TcpServerImpl extends TcpServer {

    @Inject private NodeService nodeService;
    @Inject private StoreService storeService;
    @Inject private ReplicationService replicationService;
    @Inject private SeedService seedService;

    @Override
    protected void onStart() {
        replicationService.initialize();
        nodeService.init();
        storeService.init();
        logger.info("Running {} on {}", config.name(), serverAddress());
    }

    @Override
    protected void synchronize(CRDTRequest data) {
        replicationService.synchronize(data);
    }

    @Override
    protected void contactSeeds() {
        seedService.contactSeeds();
    }
}
