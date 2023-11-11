package com.stundb.service;

import com.stundb.net.core.models.requests.DelRequest;
import com.stundb.net.core.models.requests.ExistsRequest;
import com.stundb.net.core.models.requests.GetRequest;
import com.stundb.net.core.models.requests.SetRequest;
import com.stundb.net.core.models.responses.CapacityResponse;
import com.stundb.net.core.models.responses.ExistsResponse;
import com.stundb.net.core.models.responses.GetResponse;
import com.stundb.net.core.models.responses.IsEmptyResponse;

public interface StoreService {

    void set(SetRequest request);

    void del(DelRequest request);

    GetResponse get(GetRequest request);

    IsEmptyResponse isEmpty();

    CapacityResponse capacity();

    void clear();

    ExistsResponse exists(ExistsRequest request);
}
