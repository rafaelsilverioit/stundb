package com.stundb.server.handlers.nodes;

import com.stundb.core.models.Node;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.service.NodeService;
import io.netty.channel.Channel;
import lombok.Getter;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.stream.Stream;

import static com.stundb.core.models.Status.State.RUNNING;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElectedHandlerTest extends NodeHandlerTest<ElectedHandler> {

    @Getter
    @InjectMocks
    private ElectedHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.ELECTED);
    }

    @Override
    protected Stream<Arguments> test_execute() {
        return Stream.of(
                Arguments.of(new ElectedRequest(new Node("", 8080, 1L, true, com.stundb.core.models.Status.create(RUNNING)))),
                Arguments.of((ElectedRequest) null)
        );
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
