package com.stundb.clients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public abstract class GrpcRunner<REQ, RES> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract void execute(ManagedChannel channel, REQ request, StreamObserver<RES> observer);

    @SneakyThrows
    public void run(
            String ip,
            Integer port,
            REQ request,
            StreamObserver<RES> observer) {
        ManagedChannel channel = channel(ip, port);
        execute(channel, request, observer);
        //channel.shutdown();
        //channel.shutdown();
        //channel.awaitTermination(2, TimeUnit.SECONDS);
    }

    private ManagedChannel channel(String ip, Integer port) {
        return ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
    }
}
