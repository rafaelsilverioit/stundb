package com.stundb.service;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.DeregisterResponse;
import com.stundb.net.core.models.responses.ListNodesResponse;
import com.stundb.net.core.models.responses.PingResponse;
import com.stundb.net.core.models.responses.RegisterResponse;

public interface NodeService {

    void init();

    PingResponse ping(PingRequest request);

    void trackNodeFailure(Node node);

    RegisterResponse register(Request request);

    DeregisterResponse deregister(DeregisterRequest request);

    ListNodesResponse list();

    void startElection(Request request);

    void elected(ElectedRequest request);

    void synchronize(CRDTRequest request);
}
