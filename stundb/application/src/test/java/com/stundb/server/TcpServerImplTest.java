package com.stundb.server;

import static org.mockito.Mockito.verify;

import com.stundb.BaseTest;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

class TcpServerImplTest extends BaseTest {

    @Mock private NodeService nodeService;

    @Mock private ReplicationService replicationService;

    @Mock private ApplicationConfig config;

    @InjectMocks private TcpServerImpl testee;

    @Test
    void test_onStart() {
        testee.onStart();
        verify(nodeService).init();
        verify(config).getName();
    }

    @Test
    void test_synchronize() {
        var data = new CRDTRequest(List.of(), List.of());
        testee.synchronize(data);
        verify(replicationService).synchronize(data);
    }
}
