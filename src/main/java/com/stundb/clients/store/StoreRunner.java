package com.stundb.clients.store;

import com.stundb.service.StoreServiceGrpc;
import io.grpc.ManagedChannel;

class StoreRunner {

    protected StoreServiceGrpc.StoreServiceBlockingStub getStubFor(ManagedChannel channel) {
        return StoreServiceGrpc.newBlockingStub(channel);
    }
}
