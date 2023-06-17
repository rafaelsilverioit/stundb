package com.stundb.service;

import io.grpc.stub.StreamObserver;

public interface Service {

    default <T> void onCompleted(T response, StreamObserver<T> responseObserver) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
