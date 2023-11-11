package com.stundb.service.impl;

import com.stundb.BaseTest;
import com.stundb.core.cache.Cache;
import com.stundb.core.models.Node;
import com.stundb.core.models.Status;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.Response;
import com.stundb.service.ElectionService;
import com.stundb.service.ReplicationService;
import com.stundb.utils.NodeUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NodeServiceImplTest extends BaseTest {

    @Captor
    private ArgumentCaptor<Node> captor;

    @Mock
    private StunDBClient client;
    @Mock
    private Cache<Node> internalCache;
    @Mock
    private ReplicationService replicationService;
    @Mock
    private ElectionService election;
    @Mock
    private TimerTask coordinatorTimerTask;
    @Mock
    private UniqueId uniqueId;
    @Mock
    private NodeUtils utils;
    @Mock
    private Timer timer;

    @InjectMocks
    private NodeServiceImpl testee;

    private Stream<Arguments> electedArguments() {
        return Stream.of(
                Arguments.of(Stream.empty()),
                Arguments.of(Stream.of(buildNode(Status.State.RUNNING, 321L, false))));
    }

    private Stream<Arguments> registerArguments() {
        var node = buildNode(Status.State.RUNNING, 321L, false);
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(node)));
    }

    @Test
    void init_should_work_successfully() {
        testee.init();

        verify(timer, times(1)).scheduleAtFixedRate(eq(coordinatorTimerTask), anyLong(), anyLong());
    }

    @Test
    void ping_should_work_successfully() {
        testee.ping(Request.buildRequest(Command.PING, null));

        verify(client, never()).requestAsync(any(), any(), any());
    }

    @Test
    void list_should_work_successfully() {
        when(internalCache.getAll()).thenReturn(List.of());

        var response = testee.list();

        verify(internalCache, times(1)).getAll();

        assertTrue(response.nodes().isEmpty());
    }

    @Test
    void startElection_should_work_successfully() {
        testee.startElection(Request.buildRequest(Command.START_ELECTION, null));

        verify(election, times(1)).run(true);
    }

    @Test
    void deregister_should_work_successfully() {
        testee.deregister(new DeregisterRequest(123L));

        verify(internalCache, times(1)).del("123");
    }

    @Test
    void synchronize_should_work_successfully() {
        testee.synchronize(new CRDTRequest(List.of(), List.of()));

        verify(replicationService, times(1)).synchronize(any());
    }

    @Test
    void trackNodeFailure_should_do_nothing_when_node_is_disabled() {
        testee.trackNodeFailure(buildNode(Status.State.DISABLED));

        verify(internalCache, never()).put(any(), any());
    }

    @Test
    void trackNodeFailure_should_do_nothing_when_node_has_not_failed_before() {
        testee.trackNodeFailure(buildNode(Status.State.RUNNING));

        verify(internalCache, never()).put(any(), any());
    }

    @Test
    void trackNodeFailure_should_update_node_status_when_node_has_failed_before() {
        var node = buildNode(Status.State.RUNNING);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);
        testee.trackNodeFailure(node);

        verify(internalCache, times(1)).put(any(), captor.capture());

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
        assertNotEquals(node.status(), captor.getValue().status());
        assertEquals(Status.State.FAILING, captor.getValue().status().state());
    }

    @Test
    void elected_should_update_cache_to_identify_new_cluster_leader() {
        var node = buildNode(Status.State.RUNNING);
        var currentLeader = buildNode(Status.State.RUNNING, 321L, true);

        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.put(any(), any())).thenReturn(true).thenReturn(true);

        testee.elected(new ElectedRequest(node));

        verify(utils, times(1)).filterNodesByState(any(), any(), any());
        verify(internalCache, times(2)).put(any(), captor.capture());
        verify(election, times(1)).finished();

        assertEquals(currentLeader.uniqueId(), captor.getAllValues().get(0).uniqueId());
        assertFalse(captor.getAllValues().get(0).leader());
        assertEquals(node.uniqueId(), captor.getAllValues().get(1).uniqueId());
        assertTrue(captor.getAllValues().get(1).leader());
    }

    @ParameterizedTest
    @MethodSource("electedArguments")
    void elected_should_update_cache_even_when_no_current_leader_is_found(Stream<Node> nodes) {
        var node = buildNode(Status.State.RUNNING);

        when(utils.filterNodesByState(any(), any(), any())).thenReturn(nodes);
        when(internalCache.put(any(), any())).thenReturn(true).thenReturn(true);

        testee.elected(new ElectedRequest(node));

        verify(utils, times(1)).filterNodesByState(any(), any(), any());
        verify(internalCache, times(1)).put(any(), captor.capture());
        verify(election, times(1)).finished();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
        assertTrue(captor.getValue().leader());
    }

    @Test
    void register_should_register_new_cluster_member() {
        var node = buildNode(Status.State.RUNNING, 321L, false);
        var currentLeader = buildNode(Status.State.RUNNING);
        var request = Request.buildRequest(Command.REGISTER, new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(List.of(currentLeader));
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.put(any(), any())).thenReturn(true).thenReturn(true);
        when(client.requestAsync(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(Response.buildResponse(request, com.stundb.net.core.models.Status.OK, null)));
        when(replicationService.generateCrdtRequest()).thenReturn(new CRDTRequest(List.of(), List.of()));

        testee.register(request);

        verify(utils, times(1)).filterNodesByState(any(), any(), any());
        verify(internalCache, times(1)).put(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election, never()).run();
        verify(client, times(1)).requestAsync(any(), any(), any());
        verify(replicationService, times(1)).generateCrdtRequest();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
    }

    @ParameterizedTest
    @MethodSource("registerArguments")
    void register_should_register_new_cluster_member_even_when_node_is_alone_in_the_cluster(List<Node> nodes) {
        var node = buildNode(Status.State.RUNNING, 321L, false);
        var request = Request.buildRequest(Command.REGISTER, new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(nodes);
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(nodes.stream());
        when(internalCache.put(any(), any())).thenReturn(true).thenReturn(true);
        when(replicationService.generateCrdtRequest()).thenReturn(new CRDTRequest(List.of(), List.of()));

        testee.register(request);

        verify(utils, times(1)).filterNodesByState(any(), any(), any());
        verify(internalCache, times(1)).put(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election, never()).run();
        verify(client, never()).requestAsync(any(), any(), any());
        verify(replicationService, times(1)).generateCrdtRequest();

        assertEquals(node.uniqueId(), captor.getValue().uniqueId());
    }

    @Test
    void register_should_register_new_cluster_memberxxx() {
        var node = buildNode(Status.State.RUNNING, 321L, false);
        var currentLeader = buildNode(Status.State.RUNNING);
        var request = Request.buildRequest(Command.REGISTER, new RegisterRequest(node.ip(), node.port(), node.uniqueId()));

        when(internalCache.getAll()).thenReturn(List.of(currentLeader));
        when(utils.filterNodesByState(any(), any(), any())).thenReturn(Stream.of(currentLeader));
        when(internalCache.put(any(), any())).thenReturn(true).thenReturn(true);
        when(client.requestAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new Exception()));
        when(replicationService.generateCrdtRequest()).thenReturn(new CRDTRequest(List.of(), List.of()));

        testee.register(request);

        verify(utils, times(1)).filterNodesByState(any(), any(), any());
        verify(internalCache, times(2)).put(any(), captor.capture());
        verify(internalCache, times(2)).getAll();
        verify(election, times(1)).run();
        verify(client, times(1)).requestAsync(any(), any(), any());
        verify(replicationService, times(1)).generateCrdtRequest();

        assertEquals(node.uniqueId(), captor.getAllValues().get(0).uniqueId());
        assertEquals(currentLeader.uniqueId(), captor.getAllValues().get(1).uniqueId());
        assertEquals(Status.State.FAILING, captor.getAllValues().get(1).status().state());
    }

    private Node buildNode(Status.State state) {
        return buildNode(state, 123L, true);
    }

    private Node buildNode(Status.State state, long uniqueId, boolean leader) {
        return new Node("", 8080, uniqueId, leader, Status.create(state));
    }
}
