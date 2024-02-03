package com.stundb.server;

import com.stundb.core.models.UniqueId;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.server.TcpServer;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import lombok.NoArgsConstructor;

import jakarta.inject.Inject;

@NoArgsConstructor
public class TcpServerImpl extends TcpServer {

    @Inject
    private NodeService nodeService;

    @Inject
    private ReplicationService replicationService;

    @Override
    protected void onStart() {
        nodeService.init();
    }

    @Override
    protected void synchronize(CRDTRequest data) {
        replicationService.synchronize(data);
    }
}
