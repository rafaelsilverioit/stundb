package com.stundb.server.handlers.nodes;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.DeregisterRequest;
import com.stundb.net.core.models.requests.Request;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListHandlerTest extends NodeHandlerTest<ListHandler> {

    @Getter
    @InjectMocks
    private ListHandler testee;

    private Stream<Arguments> test_isSupported() {
        return test_isSupported(Command.LIST);
    }

    @Override
    protected void verifyNodeServiceCall(Request request) {
        verify(nodeService).list();
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
    void test_execute_for_list() {
        var request = Request.buildRequest(Command.LIST, null);
        super.test_execute(request);
    }
}
