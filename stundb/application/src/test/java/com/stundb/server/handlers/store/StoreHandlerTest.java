package com.stundb.server.handlers.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.server.handlers.CommandHandler;
import com.stundb.service.StoreService;

import io.netty.channel.Channel;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.stream.Stream;

public abstract class StoreHandlerTest<T extends CommandHandler> {

    @Mock protected StoreService storeService;
    @Mock private Channel channel;
    @Captor private ArgumentCaptor<Response> captor;

    protected abstract T getTestee();

    protected abstract void verifyStoreServiceCall(Request request);

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

    protected Stream<Arguments> test_isSupported(Command command) {
        return Stream.of(Arguments.of(command, true), Arguments.of(null, false));
    }

    protected void verifyMocks(Request request) {
        verifyStoreServiceCall(request);
        verify(channel).writeAndFlush(captor.capture());
    }

    protected void assertResponse(Command command) {
        var response = captor.getValue();
        assertEquals(response.version(), Version.STUNDB_v1_0);
        assertEquals(response.status(), Status.OK);
        assertEquals(response.command(), command);
        assertEquals(response.type(), Type.RAW);
        assertNull(response.payload());
    }
}
