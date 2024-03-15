package com.stundb.server.handlers.store;

import static org.mockito.Mockito.verify;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.Request;

import lombok.Getter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@Getter
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapacityHandlerTest extends StoreHandlerTest<CapacityHandler> {

    @InjectMocks private CapacityHandler testee;

    private static Stream<Arguments> test_isSupported() {
        return Stream.of(Arguments.of(Command.CAPACITY, true), Arguments.of(null, false));
    }

    @Override
    protected void verifyStoreServiceCall(Request request) {
        verify(storeService).capacity();
    }

    @Override
    protected Stream<Arguments> test_execute() {
        throw new UnsupportedOperationException();
    }

    @ParameterizedTest
    @MethodSource("test_isSupported")
    void test_isSupported(Command command, boolean expected) {
        super.test_isSupported(command, expected);
    }

    @Test
    void test_execute_synchronize() {
        var request = Request.buildRequest(Command.CAPACITY, null);
        super.test_execute(request);
    }
}
