package com.stundb.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.stundb.api.models.Tuple;
import com.stundb.core.cache.Cache;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.Response;
import com.stundb.service.ElectionService;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NodeServiceImplTest {

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NodeServiceImpl.class);

    @Captor private ArgumentCaptor<Node> captor;
    @Mock private StunDBClient client;
    @Mock private Cache<Node> internalCache;
    @Mock private ReplicationService replicationService;
    @Mock private ElectionService election;
    @Mock private TimerTask coordinatorTimerTask;
    @Mock private UniqueId uniqueId;
    @Mock private NodeUtils utils;
    @Mock private Timer timer;
    private NodeServiceImpl testee;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        testee = new NodeServiceImpl();

        getField("client").set(testee, client);
        getField("internalCache").set(testee, internalCache);
        getField("replicationService").set(testee, replicationService);
        getField("election").set(testee, election);
        getField("coordinatorTimerTask").set(testee, coordinatorTimerTask);
        getField("uniqueId").set(testee, uniqueId);
        getField("utils").set(testee, utils);
        getField("timer").set(testee, timer);
    }

    @AfterEach
    void tearDown() {
        logger.clear();
    }

    private Stream<Arguments> electedArguments() {
        return Stream.of(
                Arguments.of(Stream.empty()),
                Arguments.of(Stream.of(buildNode(NodeStatus.State.RUNNING, 321L, false))));
    }

    private Stream<Arguments> registerArguments() {
        var node = buildNode(NodeStatus.State.RUNNING, 321L, false);
        return Stream.of(Arguments.of(List.of()), Arguments.of(List.of(node)));
    }

    @Test
    void init_should_work_successfully() {
        testee.init();

        verify(timer).scheduleAtFixedRate(eq(coordinatorTimerTask), anyLong(), anyLong());
    }

    @Test
    void ping_should_work_successfully() {
        when(replicationService.verifySynchroneity(any()))
                .thenReturn(new Tuple<>(List.of(), List.of()));
        when(internalCache.getAll()).thenReturn(List.of());

        testee.ping(new PingRequest(Map.of("randomKey", 1L)));

        verify(replicationService).verifySynchroneity(any());
        verify(internalCache).getAll();
    }

    @Test
    void list_should_work_successfully() {
        when(internalCache.getAll()).thenReturn(List.of());

        var response = testee.list();

        verify(internalCache).getAll();

        assertTrue(response.nodes().isEmpty());
    }

    @Test
    void startElection_should_work_successfully() {
        testee.startElection(Request.buildRequest(Command.START_ELECTION, null));

        verify(election).run(true);
    }

    @Test
    void deregister_should_work_successfully() {
        var response = testee.deregister(new DeregisterRequest(123L));

        verify(internalCache).del("123");
        verify(internalCache).getAll();
        assertTrue(response.nodes().isEmpty());
    }

    @Test
    void synchronize_should_work_successfully() {
        testee.synchronize(new CRDTRequest(List.of(), List.of()));

        verify(replicationService).synchronize(any(), any());
    }

    @Test
    void trackNodeFailure_should_do_nothing_when_node_is_disabled() {
        testee.trackNodeFailure(buildNode(NodeStatus.State.DISABLED));

        verify(internalCache, never()).upsert(any(), any());
    }

    @Test
    void trackNodeFailure_should_do_nothing_when_node_has_not_failed_before() {
        testee.trackNodeFailure(buildNode(NodeStatus.State.RUNNING));

        verify(internalCache, never()).upsert(any(), any());
    }

    @Test
    void trackNodeFailure_should_update_node_status_when_node_has_failed_before() {
        var node = buildNode(NodeStatus.State.RUNNING);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);

        verify(internalCache).upsert(any(), captor.capture());

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
        assertNotEquals(node.status(), captor.getValue().status());
        assertEquals(NodeStatus.State.FAILING, captor.getValue().status().state());
    }

    @Test
    void elected_should_update_cache_to_identify_new_cluster_leader() {
        var node = buildNode(NodeStatus.State.RUNNING);
        var currentLeader = buildNode(NodeStatus.State.RUNNING, 321L, true);

        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.upsert(any(), any())).thenReturn(true).thenReturn(true);

        testee.elected(new ElectedRequest(node));

        verify(utils).filterNodesByState(any(), any(), any());
        verify(internalCache, times(2)).upsert(any(), captor.capture());
        verify(election).finished();

        assertEquals(currentLeader.uniqueId(), captor.getAllValues().get(0).uniqueId());
        assertFalse(captor.getAllValues().get(0).leader());
        assertEquals(node.uniqueId(), captor.getAllValues().get(1).uniqueId());
        assertTrue(captor.getAllValues().get(1).leader());
    }

    @ParameterizedTest
    @MethodSource("electedArguments")
    void elected_should_update_cache_even_when_no_current_leader_is_found(Stream<Node> nodes) {
        var node = buildNode(NodeStatus.State.RUNNING);

        when(utils.filterNodesByState(any(), any(), any())).thenReturn(nodes);
        when(internalCache.upsert(any(), any())).thenReturn(true).thenReturn(true);

        testee.elected(new ElectedRequest(node));

        verify(utils).filterNodesByState(any(), any(), any());
        verify(internalCache).upsert(any(), captor.capture());
        verify(election).finished();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
        assertTrue(captor.getValue().leader());
    }

    @Test
    void register_should_register_new_cluster_member() {
        var node = buildNode(NodeStatus.State.RUNNING, 321L, false);
        var currentLeader = buildNode(NodeStatus.State.RUNNING);
        var request =
                Request.buildRequest(
                        Command.REGISTER,
                        new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(List.of(currentLeader));
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.upsert(any(), any())).thenReturn(true).thenReturn(true);
        when(client.requestAsync(any(), any(), any()))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                Response.buildResponse(
                                        request, com.stundb.net.core.models.Status.OK, null)));
        when(replicationService.generateStateSnapshot())
                .thenReturn(new Tuple<>(List.of(), List.of()));

        testee.register(request);

        verify(utils).filterNodesByState(any(), any(), any());
        verify(internalCache).upsert(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election, never()).run();
        verify(client).requestAsync(any(), any(), any());
        verify(replicationService).generateStateSnapshot();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
    }

    @ParameterizedTest
    @MethodSource("registerArguments")
    void register_should_register_new_cluster_member_even_when_node_is_alone_in_the_cluster(
            List<Node> nodes) {
        var node = buildNode(NodeStatus.State.RUNNING, 321L, false);
        var request =
                Request.buildRequest(
                        Command.REGISTER,
                        new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(nodes);
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(nodes.stream());
        when(internalCache.upsert(any(), any())).thenReturn(true).thenReturn(true);
        when(replicationService.generateStateSnapshot())
                .thenReturn(new Tuple<>(List.of(), List.of()));

        testee.register(request);

        verify(utils).filterNodesByState(any(), any(), any());
        verify(internalCache).upsert(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election, never()).run();
        verify(client, never()).requestAsync(any(), any(), any());
        verify(replicationService).generateStateSnapshot();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
    }

    @Test
    void register_should_mark_seed_node_as_failing_when_communication_fails() {
        var node = buildNode(NodeStatus.State.RUNNING, 321L, false);
        var currentLeader = buildNode(NodeStatus.State.RUNNING);
        var request =
                Request.buildRequest(
                        Command.REGISTER,
                        new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(List.of(currentLeader));
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.upsert(any(), any())).thenReturn(true).thenReturn(true);
        when(client.requestAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new Exception("dummy exception")));
        when(replicationService.generateStateSnapshot())
                .thenReturn(new Tuple<>(List.of(), List.of()));

        testee.register(request);

        verify(utils).filterNodesByState(any(), any(), any());
        verify(internalCache, times(2)).upsert(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election).run();
        verify(client).requestAsync(any(), any(), any());
        verify(replicationService).generateStateSnapshot();

        assertThat(logger.getLoggingEvents(), hasSize(1));
        assertEquals(node.uniqueId(), captor.getAllValues().get(0).uniqueId());
        assertEquals(currentLeader.uniqueId(), captor.getAllValues().get(1).uniqueId());
        assertEquals(NodeStatus.State.FAILING, captor.getAllValues().get(1).status().state());
    }

    private Node buildNode(NodeStatus.State state) {
        return buildNode(state, 123L, true);
    }

    private Node buildNode(NodeStatus.State state, long uniqueId, boolean leader) {
        return new Node("", 8080, uniqueId, leader, NodeStatus.create(state));
    }

    private Field getField(String fieldName) throws NoSuchFieldException {
        var field = NodeServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
