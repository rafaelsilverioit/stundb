package com.stundb.server.handlers.nodes;

import static org.mockito.ArgumentMatchers.any;
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
public class PingHandlerTest extends NodeHandlerTest<PingHandler> {

    @InjectMocks private PingHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.PING);
    }

    @Override
    protected void verifyNodeServiceCall(Request request) {
        verify(nodeService).ping(any());
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
    void test_execute_ping() {
        var request = Request.buildRequest(Command.PING, null);
        super.test_execute(request);
    }
}
