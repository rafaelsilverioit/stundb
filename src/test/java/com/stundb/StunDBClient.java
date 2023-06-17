package com.stundb;

import com.google.protobuf.ByteString;
import com.stundb.service.GetRequest;
import com.stundb.service.StoreServiceGrpc;
import com.stundb.service.SetRequest;
import io.grpc.ManagedChannelBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StunDBClient {

    public static void main(String[] args) throws IOException {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8000)
                .usePlaintext()
                .build();

        var stub = StoreServiceGrpc.newBlockingStub(channel);
        var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("img.jpeg");

        stub.set(SetRequest.newBuilder()
                .setKey("realngnx")
                .setValue(ByteString.readFrom(is))
                .build());

        var response = stub.get(GetRequest.newBuilder()
                .setKey("realngnx")
                .build());

        var inputStream = response.getValue().newInput();
        var file = new File("src/test/resources/output.jpeg");
        Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);
        inputStream.close();

        channel.shutdown();
    }
}
