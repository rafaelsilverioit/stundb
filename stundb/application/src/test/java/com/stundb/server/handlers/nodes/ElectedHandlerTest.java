package com.stundb.server.handlers.nodes;

import static com.stundb.net.core.models.NodeStatus.State.RUNNING;

import static org.mockito.Mockito.verify;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;

import lombok.Getter;

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
public class ElectedHandlerTest extends NodeHandlerTest<ElectedHandler> {

    @InjectMocks private ElectedHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.ELECTED);
    }

    @Override
    protected Stream<Arguments> test_execute() {
        return Stream.of(
                Arguments.of(
                        new ElectedRequest(
                                new Node("", 8080, 1L, true, NodeStatus.create(RUNNING)))),
                Arguments.of((ElectedRequest) null));
    }

    @Override
    protected void verifyNodeServiceCall(Request request) {
        verify(nodeService).elected((ElectedRequest) request.payload());
    }

    @Override
    @ParameterizedTest
    @MethodSource("test_isSupported")
    void test_isSupported(Command command, boolean expected) {
        super.test_isSupported(command, expected);
    }

    @ParameterizedTest
    @MethodSource("test_execute")
    void test_execute(Object payload) {
        var request = Request.buildRequest(Command.ELECTED, payload);
        super.test_execute(request);
    }
}
