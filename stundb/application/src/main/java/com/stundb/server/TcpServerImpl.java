package com.stundb.server;

import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.server.TcpServer;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import com.stundb.service.SeedService;

import jakarta.inject.Inject;

public class TcpServerImpl extends TcpServer {

    @Inject private NodeService nodeService;
    @Inject private ReplicationService replicationService;
    @Inject private SeedService seedService;

    @Override
    protected void onStart() {
        nodeService.init();
        logger.info("Running {} on {}", config.getName(), serverAddress());
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
