package com.stundb.timers.impl;

import static com.stundb.net.core.models.NodeStatus.State.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.stundb.core.cache.Cache;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.DeregisterResponse;
import com.stundb.net.core.models.responses.PingResponse;
import com.stundb.net.core.models.responses.Response;
import com.stundb.service.ElectionService;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class CoordinatorTimerTaskImplTest {

    private static final Node NODE =
            new Node("127.0.0.1", 9000, 1L, true, NodeStatus.create(RUNNING));
    private final TestLogger logger =
            TestLoggerFactory.getTestLogger(CoordinatorTimerTaskImpl.class);

    @Mock private Cache<Node> internalCache;
    @Mock private ElectionService election;
    @Mock private StunDBClient client;
    @Mock private ReplicationService replicationService;
    @Mock private UniqueId uniqueId;
    @Mock private NodeUtils utils;

    private CoordinatorTimerTaskImpl testee;

    private static Stream<Arguments> run_arguments_ping() {
        return Stream.of(
                Arguments.of(
                        CompletableFuture.completedFuture(
                                Response.buildResponse(
                                        new Request(null, null, null, null, null, null),
                                        Status.OK,
                                        new PingResponse(List.of(), List.of(), List.of(NODE)))),
                        1,
                        0),
                Arguments.of(CompletableFuture.failedFuture(new RuntimeException()), 0, 1),
                Arguments.of(CompletableFuture.failedFuture(new RuntimeException()), 0, 1));
    }

    private static Stream<Arguments> run_arguments_deregister() {
        return Stream.of(
                Arguments.of(
                        CompletableFuture.completedFuture(
                                Response.buildResponse(
                                        new Request(null, null, null, null, null, null),
                                        Status.OK,
                                        new DeregisterResponse(List.of()))),
                        0),
                Arguments.of(CompletableFuture.failedFuture(new RuntimeException()), 1));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        testee = new CoordinatorTimerTaskImpl();

        getField("internalCache").set(testee, internalCache);
        getField("election").set(testee, election);
        getField("client").set(testee, client);
        getField("replicationService").set(testee, replicationService);
        getField("uniqueId").set(testee, uniqueId);
        getField("utils").set(testee, utils);
    }

    @AfterEach
    void tearDown() {
        logger.clear();
    }

    @ParameterizedTest
    @MethodSource("run_arguments_ping")
    void run_when_not_current_leader(
            CompletableFuture<Response> future,
            Integer expectedSynchronizeCalls,
            Integer numberOfLogEntries) {
        when(internalCache.get(any())).thenReturn(Optional.empty());
        when(utils.filterNodesByState(anyCollection(), anyLong(), anyList()))
                .thenReturn(Stream.of(NODE));
        when(client.requestAsync(any(Command.class), any(), anyString(), anyInt()))
                .thenReturn(future);

        testee.run();

        verify(internalCache).get(any());
        verify(uniqueId).text();
        verify(utils).filterNodesByState(anyCollection(), anyLong(), anyList());
        verify(internalCache).getAll();
        verify(uniqueId).number();
        verify(election, never()).run();
        verify(client).requestAsync(any(Command.class), any(), anyString(), anyInt());
        verify(replicationService).generateVersionClock();
        verify(replicationService, times(expectedSynchronizeCalls))
                .synchronize(anyCollection(), anyCollection());
        verify(internalCache).upsert(anyString(), any());

        assertThat(logger.getLoggingEvents(), hasSize(numberOfLogEntries));
    }

    @ParameterizedTest
    @MethodSource("run_arguments_ping")
    void run_when_current_leader(CompletableFuture<Response> future) {
        when(internalCache.get(any())).thenReturn(Optional.of(NODE));
        when(internalCache.getAll()).thenReturn(List.of(NODE.clone(FAILING)));
        when(utils.filterNodesByState(anyCollection(), anyLong(), anyList()))
                .thenReturn(Stream.of(NODE.clone(FAILING)));
        when(client.requestAsync(any(Command.class), any(), anyString(), anyInt()))
                .thenReturn(future);

        testee.run();

        verify(internalCache).get(any());
        verify(uniqueId).text();
        verify(utils).filterNodesByState(anyCollection(), anyLong(), anyList());
        verify(internalCache).getAll();
        verify(uniqueId).number();
        verify(election, never()).run();
        verify(client).requestAsync(any(Command.class), any(), anyString(), anyInt());
        verify(internalCache, never()).del(any());
        verify(replicationService).generateVersionClock();
        verify(internalCache).upsert(anyString(), any());
    }

    @ParameterizedTest
    @MethodSource("run_arguments_deregister")
    void run_when_current_leader_and_has_disabled_nodes(
            CompletableFuture<Response> future, Integer expectedUpsertCalls) {
        var node = new Node("127.0.0.1", 9000, 2L, false, NodeStatus.create(DISABLED));
        when(internalCache.get(any())).thenReturn(Optional.of(NODE));
        when(internalCache.getAll()).thenReturn(List.of(node));
        when(utils.filterNodesByState(anyCollection(), anyLong(), anyList()))
                .thenReturn(Stream.of(node))
                .thenReturn(Stream.of(node, NODE));
        when(client.requestAsync(any(Command.class), any(), anyString(), anyInt()))
                .thenReturn(future);

        testee.run();

        verify(internalCache).get(any());
        verify(uniqueId).text();
        verify(utils, times(2)).filterNodesByState(anyCollection(), anyLong(), anyList());
        verify(internalCache).getAll();
        verify(uniqueId, times(2)).number();
        verify(election, never()).run();
        verify(client).requestAsync(any(Command.class), any(), anyString(), anyInt());
        verify(internalCache).del(any());
        verify(internalCache, times(expectedUpsertCalls)).upsert(anyString(), any());
    }

    private Field getField(String fieldName) throws NoSuchFieldException {
        var field = CoordinatorTimerTaskImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
