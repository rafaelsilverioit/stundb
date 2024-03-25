package com.stundb.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.core.cache.Cache;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;
import com.stundb.utils.NodeUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElectionServiceImplTest {

    private static final String NODE_UNIQUE_TEXT_ID = "123456";
    private static final Long NODE_UNIQUE_ID = 123456L;
    private static final String NODE_IP = "0.0.0.0";
    private static final int NODE_PORT = 8000;
    private static final long ANOTHER_NODE_ID = 654321L;

    @Captor private ArgumentCaptor<Request> requestCaptor;
    @Captor private ArgumentCaptor<Node> nodeCaptor;
    @Mock private Cache<Node> internalCache;
    @Mock private StunDBClient client;
    @Mock private ApplicationConfig config;
    @Mock private UniqueId uniqueId;
    @Mock private NodeUtils utils;
    @InjectMocks private ElectionServiceImpl testee;

    @Test
    void test_finished() throws NoSuchFieldException, IllegalAccessException {
        var field = getElectionStartedField();
        field.set(testee, new AtomicBoolean(true));

        testee.finished();

        var electionStarted = (AtomicBoolean) field.get(testee);
        assertFalse(electionStarted.get());
    }

    @ParameterizedTest
    @MethodSource("startElectionArguments")
    void test_startElection(Node node, Boolean force, Boolean expected)
            throws NoSuchFieldException, IllegalAccessException {
        when(internalCache.get(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID))
                .thenReturn(Optional.ofNullable(node));
        when(uniqueId.text()).thenReturn(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID);

        testee.run(force);

        verify(client, never()).requestAsync(any(Request.class), eq(NODE_IP), eq(NODE_PORT));

        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        assertEquals(electionStarted.get(), expected);
    }

    @Test
    void test_startElection_when_election_already_started_and_force_is_false()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        electionStarted.set(true);
        var node = aNode(NODE_UNIQUE_ID, false);
        test_startElection(node, false, true);
    }

    @Test
    void test_startElection_when_election_already_started_and_force_is_falsexxxxxxx()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var counter = (AtomicInteger) getField("counter").get(testee);

        var node = aNode(NODE_UNIQUE_ID, false);

        when(internalCache.get(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID))
                .thenReturn(Optional.of(node));
        when(uniqueId.text()).thenReturn(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID);

        for (var i = 0; i < 4; i++) {
            testee.run(false);
        }

        verify(client, never()).requestAsync(any(Request.class), eq(NODE_IP), eq(NODE_PORT));

        assertFalse(electionStarted.get());
        assertEquals(counter.get(), 4);
    }

    @Test
    void test_startElection_when_internal_cache_contains_only_a_single_node_4()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        electionStarted.set(true);
        var node = aNode(NODE_UNIQUE_ID, false);
        test_startElection(node, false, true);
    }

    @Test
    void test_startElection_when_the_only_other_node_with_greater_unique_id_is_unavailable()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(ANOTHER_NODE_ID, false);

        mockBasicOperations(List.of(node, anotherNode), aResponse(null, true));

        testee.run(true);

        verify(client).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(config, never()).ip();
        verify(config, never()).port();
        verify(internalCache).upsert(any(), nodeCaptor.capture());

        assertTrue(electionStarted.get());
        assertNull(requestCaptor.getValue().payload());
        assertEquals(nodeCaptor.getValue().status().state(), NodeStatus.State.FAILING);
    }

    @Test
    void
            test_startElection_when_the_only_other_node_to_be_notified_about_election_result_is_unavailable()
                    throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(NODE_UNIQUE_ID - 1, false);

        mockAllOperations(List.of(node, anotherNode), aResponse(null, true));

        testee.run(true);

        verify(client).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(internalCache, times(2)).upsert(any(), nodeCaptor.capture());

        assertFalse(electionStarted.get());
        var payload = (ElectedRequest) requestCaptor.getValue().payload();
        assertNotNull(payload);
        assertTrue(payload.leader().leader());
        assertEquals(payload.leader().uniqueId(), NODE_UNIQUE_ID);
        assertEquals(nodeCaptor.getAllValues().get(0).status().state(), NodeStatus.State.FAILING);
        assertTrue(nodeCaptor.getAllValues().get(1).leader());
    }

    @Test
    void test_startElection_when_one_other_node_with_greater_unique_id_is_available()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(ANOTHER_NODE_ID, false);

        mockBasicOperations(List.of(node, anotherNode), aResponse(Command.START_ELECTION, false));

        testee.run(true);

        verify(client).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(config, never()).ip();
        verify(config, never()).port();
        verify(internalCache, never()).upsert(any(), any());

        assertTrue(electionStarted.get());
        assertNull(requestCaptor.getValue().payload());
    }

    @Test
    void test_startElection_when_no_other_node_with_greater_unique_id_is_available()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(NODE_UNIQUE_ID - 1, false);

        mockAllOperations(List.of(node, anotherNode), aResponse(Command.ELECTED, false));

        testee.run(true);

        verify(client).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(internalCache).upsert(any(), nodeCaptor.capture());

        assertFalse(electionStarted.get());
        var payload = (ElectedRequest) requestCaptor.getValue().payload();
        assertNotNull(payload);
        assertTrue(payload.leader().leader());
        assertEquals(payload.leader().uniqueId(), NODE_UNIQUE_ID);
        assertEquals(nodeCaptor.getValue().uniqueId(), NODE_UNIQUE_ID);
        assertTrue(nodeCaptor.getValue().leader());
    }

    @Test
    void test_startElection_when_another_node_is_already_the_leader()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(NODE_UNIQUE_ID - 1, false);
        var yetAnotherNode = aNode(NODE_UNIQUE_ID - 2, true);

        mockAllOperations(
                List.of(node, anotherNode, yetAnotherNode), aResponse(Command.ELECTED, false));

        testee.run(true);

        verify(client, times(2)).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(internalCache, times(2)).upsert(any(), nodeCaptor.capture());

        assertFalse(electionStarted.get());
        var payload = (ElectedRequest) requestCaptor.getValue().payload();
        assertNotNull(payload);
        assertTrue(payload.leader().leader());
        assertEquals(payload.leader().uniqueId(), NODE_UNIQUE_ID);

        var leader = nodeCaptor.getAllValues().get(0);
        assertEquals(leader.uniqueId(), NODE_UNIQUE_ID);
        assertTrue(leader.leader());

        var oldLeader = nodeCaptor.getAllValues().get(1);
        assertEquals(oldLeader.uniqueId(), NODE_UNIQUE_ID - 2);
        assertFalse(oldLeader.leader());
    }

    @Test
    void test_startElection_when_other_nodes_are_failing()
            throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(ANOTHER_NODE_ID, false, NodeStatus.State.FAILING);

        mockAllOperations(List.of(node, anotherNode), aResponse(Command.ELECTED, false));

        testee.run(true);

        verify(client).requestAsync(requestCaptor.capture(), eq(NODE_IP), eq(NODE_PORT));
        verify(internalCache).upsert(any(), nodeCaptor.capture());

        assertFalse(electionStarted.get());
        var payload = (ElectedRequest) requestCaptor.getValue().payload();
        assertNotNull(payload);
        assertTrue(payload.leader().leader());
        assertEquals(payload.leader().uniqueId(), NODE_UNIQUE_ID);
        assertEquals(nodeCaptor.getValue().uniqueId(), NODE_UNIQUE_ID);
        assertTrue(nodeCaptor.getValue().leader());
    }

    @Test
    void
            test_startElection_when_other_nodes_are_failing_and_election_was_finished_by_another_thread()
                    throws NoSuchFieldException, IllegalAccessException {
        var electionStarted = (AtomicBoolean) getElectionStartedField().get(testee);
        var node = aNode(NODE_UNIQUE_ID, false);
        var anotherNode = aNode(ANOTHER_NODE_ID, false, NodeStatus.State.RUNNING);

        when(internalCache.get(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID))
                .thenReturn(Optional.of(node));
        when(uniqueId.text()).thenReturn(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID);
        when(uniqueId.number())
                .thenReturn(Long.valueOf(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID));

        when(internalCache.getAll())
                .thenAnswer(
                        invocation -> {
                            electionStarted.set(false);
                            return List.of(node, anotherNode);
                        });

        testee.run(true);

        verify(client, never()).requestAsync(any(), eq(NODE_IP), eq(NODE_PORT));
        verify(internalCache, never()).upsert(any(), any());

        assertFalse(electionStarted.get());
    }

    private Stream<Arguments> startElectionArguments() {
        return Stream.of(
                Arguments.of(null, true, false),
                Arguments.of(aNode(NODE_UNIQUE_ID, false), true, false),
                Arguments.of(aNode(NODE_UNIQUE_ID, false), false, false),
                Arguments.of(aNode(NODE_UNIQUE_ID, true), false, false),
                Arguments.of(aNode(NODE_UNIQUE_ID, true), true, false));
    }

    private Node aNode(Long uniqueId, Boolean leader) {
        return aNode(uniqueId, leader, NodeStatus.State.RUNNING);
    }

    private Node aNode(Long uniqueId, Boolean leader, NodeStatus.State state) {
        return new Node(NODE_IP, NODE_PORT, uniqueId, leader, NodeStatus.create(state));
    }

    private void mockBasicOperations(
            List<Node> internalCacheNodes, CompletableFuture<Response> clientResponse) {
        when(client.requestAsync(any(Request.class), any(String.class), any(Integer.class)))
                .thenReturn(clientResponse);
        when(internalCache.getAll()).thenReturn(internalCacheNodes);
        when(internalCache.get(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID))
                .thenReturn(
                        internalCacheNodes.stream()
                                .filter(n -> NODE_UNIQUE_ID.equals(n.uniqueId()))
                                .findAny());
        when(uniqueId.text()).thenReturn(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID);
        when(uniqueId.number())
                .thenReturn(Long.valueOf(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID));
    }

    private void mockAllOperations(
            List<Node> internalCacheNodes, CompletableFuture<Response> clientResponse) {
        mockBasicOperations(internalCacheNodes, clientResponse);
        when(config.ip()).thenReturn(NODE_IP);
        when(config.port()).thenReturn(NODE_PORT);
        var values =
                internalCacheNodes.stream()
                        .filter(
                                n ->
                                        !Long.valueOf(ElectionServiceImplTest.NODE_UNIQUE_TEXT_ID)
                                                .equals(n.uniqueId()))
                        .toList();
        when(utils.filterNodesByState(any(), any(), any()))
                .thenReturn(values.stream())
                .thenReturn(values.stream());
    }

    private CompletableFuture<Response> aResponse(Command command, Boolean exceptionallyCompleted) {
        if (exceptionallyCompleted) {
            return CompletableFuture.failedFuture(new RuntimeException("Unexpected failure"));
        }
        return CompletableFuture.completedFuture(
                Response.buildResponse(Request.buildRequest(command, null), null, null));
    }

    private Field getElectionStartedField() throws NoSuchFieldException {
        return getField("electionStarted");
    }

    private Field getField(String fieldName) throws NoSuchFieldException {
        var field = ElectionServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
