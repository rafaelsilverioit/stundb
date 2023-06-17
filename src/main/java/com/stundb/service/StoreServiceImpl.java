package com.stundb.service;

import com.google.protobuf.ByteString;
import com.stundb.cache.Cache;
import com.stundb.logging.Loggable;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;

@Singleton
public class StoreServiceImpl
        extends StoreServiceGrpc.StoreServiceImplBase
        implements Service {

    @Inject
    private Cache<String> cache;

    @Inject
    private ReplicationService replicationService;

    @Loggable
    @Override
    public void set(SetRequest request, StreamObserver<SetResponse> responseObserver) {
        var encoder = Base64.getEncoder();
        var encoded = encoder.encodeToString(request.getValue().toByteArray());
        var response = SetResponse.newBuilder()
                .setStatus(cache.put(request.getKey(), encoded))
                .build();
        replicationService.add(request.getKey(), encoded);
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void del(DelRequest request, StreamObserver<DelResponse> responseObserver) {
        var response = DelResponse.newBuilder()
                .setStatus(cache.del(request.getKey()))
                .build();
        replicationService.remove(request.getKey());
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        var decoder = Base64.getDecoder();
        var data = cache.get(request.getKey())
                .map(decoder::decode)
                .map(ByteString::copyFrom)
                .orElse(ByteString.EMPTY);
        var response = GetResponse.newBuilder()
                .setValue(data)
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void isEmpty(IsEmptyRequest request, StreamObserver<IsEmptyResponse> responseObserver) {
        var response = IsEmptyResponse.newBuilder()
                .setIsEmpty(cache.isEmpty())
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void capacity(CapacityRequest request, StreamObserver<CapacityResponse> responseObserver) {
        var response = CapacityResponse.newBuilder()
                .setCapacity(cache.capacity())
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void clear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {
        cache.clear();
        var response = ClearResponse.newBuilder()
                .setStatus(true)
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void exists(ExistsRequest request, StreamObserver<ExistsResponse> responseObserver) {
        var response = ExistsResponse.newBuilder()
                .setExists(cache.get(request.getKey()).isPresent())
                .build();
        onCompleted(response, responseObserver);
    }
}
