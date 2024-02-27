package com.stundb.service;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.core.models.requests.DeregisterRequest;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ListNodesResponse;
import com.stundb.net.core.models.responses.RegisterResponse;

public interface NodeService {

    void init();

    void ping(Request request);

    void trackNodeFailure(Node node);

    RegisterResponse register(Request request);

    void deregister(DeregisterRequest request);

    ListNodesResponse list();

    void startElection(Request request);

    void elected(ElectedRequest request);

    void synchronize(CRDTRequest request);
}
