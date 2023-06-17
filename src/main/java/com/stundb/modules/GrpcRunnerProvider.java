package com.stundb.modules;

import com.stundb.clients.GrpcRunner;
import com.stundb.clients.node.*;
import com.stundb.clients.store.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;

@Singleton
class GrpcRunnerProvider
        implements Provider<List<GrpcRunner<? extends com.google.protobuf.GeneratedMessageV3, ? extends com.google.protobuf.GeneratedMessageV3>>> {

    @Inject
    private GetRunner getRunner;

    @Inject
    private SetRunner setRunner;

    @Inject
    private DelRunner delRunner;

    @Inject
    private CapacityRunner capacityRunner;

    @Inject
    private IsEmptyRunner isEmptyRunner;

    @Inject
    private ClearRunner clearRunner;

    @Inject
    private ExistsRunner existsRunner;

    @Inject
    private RegisterRunner registerRunner;

    @Inject
    private PingRunner pingRunner;

    @Inject
    private StartElectionRunner startElectionRunner;

    @Inject
    private ListNodesRunner listNodesRunner;

    @Inject
    private ElectedRunner electedRunner;

    @Inject
    private SynchronizationRunner synchronizationRunner;

    @Override
    public List<GrpcRunner<? extends com.google.protobuf.GeneratedMessageV3, ? extends com.google.protobuf.GeneratedMessageV3>> get() {
        return List.of(
                getRunner,
                setRunner,
                delRunner,
                capacityRunner,
                isEmptyRunner,
                clearRunner,
                existsRunner,
                registerRunner,
                pingRunner,
                startElectionRunner,
                listNodesRunner,
                electedRunner,
                synchronizationRunner);
    }
}
