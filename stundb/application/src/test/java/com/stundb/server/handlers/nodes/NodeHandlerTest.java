package com.stundb.server.handlers.nodes;

import com.stundb.BaseTest;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.NodeService;
import io.netty.channel.Channel;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

public abstract class NodeHandlerTest<T extends CommandHandler> extends BaseTest {

    @Mock
    private Channel channel;

    @Mock
    protected NodeService nodeService;

    @Captor
    private ArgumentCaptor<Response> captor;

    protected abstract T getTestee();

    protected Stream<Arguments> test_isSupported(Command command) {
        return Stream.of(
                Arguments.of(command, true),
                Arguments.of(null, false)
        );
    }

    protected abstract Stream<Arguments> test_execute();

    void test_isSupported(Command command, boolean expected) {
        var supported = getTestee().isSupported(Request.buildRequest(command, null));
        assertEquals(supported, expected);
    }

    void test_execute(Request request) {
        getTestee().execute(request, channel);

        verifyMocks(request);

        assertResponse(request.command());
    }

    protected void verifyMocks(Request request) {
        verifyNodeServiceCall(request);
        verify(channel).writeAndFlush(captor.capture());
    }

    protected abstract void verifyNodeServiceCall(Request request);

    protected void assertResponse(Command command) {
        var response = captor.getValue();
        assertEquals(response.version(), Version.STUNDB_v1_0);
        assertEquals(response.status(), Status.OK);
        assertEquals(response.command(), command);
        assertEquals(response.type(), Type.RAW);
        assertNull(response.payload());
    }
}
