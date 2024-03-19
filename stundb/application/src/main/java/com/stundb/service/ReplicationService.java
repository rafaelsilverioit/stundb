package com.stundb.service;

import com.stundb.net.core.models.requests.CRDTRequest;

public interface ReplicationService {

    void initialize();

    void synchronize(CRDTRequest request);

    CRDTRequest generateCrdtRequest();

    void add(String key, Object value);

    void remove(String key);
}
