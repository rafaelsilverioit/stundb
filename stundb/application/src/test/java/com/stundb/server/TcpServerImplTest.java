package com.stundb.server;

import static org.mockito.Mockito.verify;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.service.NodeService;
import com.stundb.service.ReplicationService;
import com.stundb.service.SeedService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TcpServerImplTest {

    @Mock private NodeService nodeService;

    @Mock private ReplicationService replicationService;

    @Mock private SeedService seedService;

    @Mock private ApplicationConfig config;

    @InjectMocks private TcpServerImpl testee;

    @Test
    void test_onStart() {
        testee.onStart();
        verify(nodeService).init();
        verify(config).name();
        verify(replicationService).initialize();
    }

    @Test
    void test_synchronize() {
        var data = new CRDTRequest(List.of(), List.of());
        testee.synchronize(data);
        verify(replicationService).synchronize(data);
    }

    @Test
    void test_contactSeeds() {
        testee.contactSeeds();
        verify(seedService).contactSeeds();
    }
}
