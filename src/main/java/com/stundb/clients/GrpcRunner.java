package com.stundb.clients;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;

public interface GrpcRunner<REQ, RES> {

    Boolean isSupported(Object request);

    RES execute(ManagedChannel channel, GeneratedMessageV3 request);
}
