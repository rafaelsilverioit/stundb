package com.stundb.observers;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class NoOpObserver<T extends GeneratedMessageV3> implements StreamObserver<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onNext(T value) {

    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    @Override
    public void onCompleted() {

    }
}
