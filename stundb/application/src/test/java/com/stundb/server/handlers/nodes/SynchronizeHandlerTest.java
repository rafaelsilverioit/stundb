package com.stundb.server.handlers.nodes;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.requests.Request;
import lombok.Getter;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SynchronizeHandlerTest extends NodeHandlerTest<SynchronizeHandler> {

    @Getter
    @InjectMocks
    private SynchronizeHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.SYNCHRONIZE);
    }

    @Override
    protected void verifyNodeServiceCall(Request request) {
        verify(nodeService).synchronize((CRDTRequest) request.payload());
    }

    @Override
    protected Stream<Arguments> test_execute() {
        return Stream.of(
                Arguments.of(new CRDTRequest(List.of(), List.of())),
                Arguments.of((CRDTRequest) null)
        );
    }

    @ParameterizedTest
    @MethodSource("test_isSupported")
    void test_isSupported(Command command, boolean expected) {
        super.test_isSupported(command, expected);
    }

    @ParameterizedTest
    @MethodSource("test_execute")
    void test_execute(Object payload) {
        var request = Request.buildRequest(Command.SYNCHRONIZE, payload);
        super.test_execute(request);
    }
}
