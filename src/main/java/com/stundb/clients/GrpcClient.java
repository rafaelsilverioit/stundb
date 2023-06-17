package com.stundb.clients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GrpcClient {

    @Inject
    private List<GrpcRunner<? extends com.google.protobuf.GeneratedMessageV3, ? extends com.google.protobuf.GeneratedMessageV3>> runners;

    private Optional<? extends com.google.protobuf.GeneratedMessageV3> execute(
            // TODO: how to avoid having to pass GeneratedMessageV3 downstream?
            ManagedChannel channel, com.google.protobuf.GeneratedMessageV3 request) {
        return runners.stream()
                .filter(runner -> runner.isSupported(request))
                .findFirst()
                .map(runner -> runner.execute(channel, request));
    }

    private ManagedChannel channel(String ip, Integer port) {
        return ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
    }

    @SneakyThrows
    public Optional<? extends com.google.protobuf.GeneratedMessageV3> run(
            String ip, Integer port, com.google.protobuf.GeneratedMessageV3 request) {
        ManagedChannel channel = channel(ip, port);
        var response = execute(channel, request);
        channel.shutdown();
        channel.awaitTermination(2, TimeUnit.SECONDS);
        return response;
    }
}
