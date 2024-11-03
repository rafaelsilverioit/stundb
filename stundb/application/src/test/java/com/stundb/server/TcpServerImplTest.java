package com.stundb.server;

import static org.mockito.Mockito.verify;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.service.NodeService;
import com.stundb.service.SeedService;
import com.stundb.service.StoreService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TcpServerImplTest {

    @Mock private NodeService nodeService;

    @Mock private StoreService storeService;

    @Mock private SeedService seedService;

    @Mock private ApplicationConfig config;

    @InjectMocks private TcpServerImpl testee;

    @Test
    void test_onStart() {
        testee.onStart();
        verify(config).name();
        verify(nodeService).init();
        verify(storeService).init();
    }

    @Test
    void test_contactSeeds() {
        testee.contactSeeds();
        verify(seedService).contactSeeds();
    }
}
