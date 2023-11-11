package com.stundb.server.handlers.store;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.ExistsRequest;
import com.stundb.net.core.models.requests.GetRequest;
import com.stundb.net.core.models.requests.Request;
import lombok.Getter;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExistsHandlerTest extends StoreHandlerTest<ExistsHandler> {

    @Getter
    @InjectMocks
    private ExistsHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.EXISTS);
    }

    @Override
    protected Stream<Arguments> test_execute() {
        return Stream.of(
                Arguments.of(new ExistsRequest("key")),
                Arguments.of((ExistsRequest) null)
        );
    }

    @Override
    protected void verifyStoreServiceCall(Request request) {
        verify(storeService).exists((ExistsRequest) request.payload());
    }

    @ParameterizedTest
    @MethodSource("test_isSupported")
    void test_isSupported(Command command, boolean expected) {
        super.test_isSupported(command, expected);
    }

    @ParameterizedTest
    @MethodSource("test_execute")
    void test_execute(Object payload) {
        var request = Request.buildRequest(Command.EXISTS, payload);
        super.test_execute(request);
    }
}
