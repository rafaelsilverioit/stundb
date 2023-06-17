package com.stundb.clients.store;

import com.stundb.service.KVStoreServiceGrpc;
import io.grpc.ManagedChannel;

class StoreRunner {

    protected KVStoreServiceGrpc.KVStoreServiceBlockingStub getStubFor(ManagedChannel channel) {
        return KVStoreServiceGrpc.newBlockingStub(channel);
    }
}
