package com.stundb.server;

import com.stundb.BaseTest;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.verify;

class TcpServerImplTest extends BaseTest {

    @Mock
    private NodeService nodeService;

    @Mock
    private ReplicationService replicationService;

    @InjectMocks
    private TcpServerImpl testee;

    @Test
    void test_onStart() {
        testee.onStart();
        verify(nodeService).init();
    }

    @Test
    void test_synchronize() {
        var data = new CRDTRequest(List.of(), List.of());
        testee.synchronize(data);
        verify(replicationService).synchronize(data);
    }
}
