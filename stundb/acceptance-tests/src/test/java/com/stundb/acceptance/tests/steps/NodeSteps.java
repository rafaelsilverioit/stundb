package com.stundb.acceptance.tests.steps;

import static com.stundb.acceptance.tests.state.NodeState.getLeader;
import static com.stundb.net.core.models.Command.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static java.util.Optional.ofNullable;

import com.stundb.acceptance.tests.state.NodeState;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.DeregisterRequest;
import com.stundb.net.core.models.requests.ElectedRequest;
import com.stundb.net.core.models.requests.GetRequest;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.responses.*;

import io.cucumber.java8.En;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.function.BiConsumer;

public class NodeSteps extends BaseSteps implements En {

    @Inject private NodeState nodeState;

    private final BiConsumer<Response, Throwable> defaultListNodesResponseHandler =
            defaultHandler.andThen(
                    (response, error) ->
                            nodeState.setNodes(extractNodesFromListNodesResponse(response)));

    private final BiConsumer<Response, Throwable> verifyNodeAddedToClusterHandler =
            defaultListNodesResponseHandler.andThen(
                    (response, error) ->
                            assertThat(nodeState.isPresent(nodeState.getNodeId()), is(true)));

    public NodeSteps() {
        super();

        BeforeStep("@timed", () -> Thread.sleep(1000L));

        Before(
                "@cluster",
                () -> requestListOfNodes(defaultNodeId, defaultListNodesResponseHandler));

        After("@cluster", () -> ofNullable(nodeState.getNodeId()).ifPresent(this::updateNodeState));

        Given("Another node running on port {int} joins the cluster", this::addNodeToCluster);

        And(
                "We are able to retrieve the current value for the key {string} from the same node",
                (String key) -> request(GET, new GetRequest(key), defaultNodeId));

        And(
                "We are able to retrieve the current value for the key {string} from node {long}",
                (String key, Long nodeId) -> request(GET, new GetRequest(key), nodeId));

        And(
                "No records are found for the key {string} in any nodes",
                this::verifyKeyIsDeletedAcrossTheCluster);

        And(
                "We are able to talk to the leader node",
                () -> requestListOfNodes(getLeaderNodeId(), defaultListNodesResponseHandler));

        And(
                "We are able to talk to the leader node and verify that node was added to the cluster",
                () -> requestListOfNodes(getLeaderNodeId(), verifyNodeAddedToClusterHandler));

        When(
                "We are able to trigger an election",
                () -> request(START_ELECTION, null, defaultNodeId));

        Then(
                "The node {long} is elected as the leader node",
                (Long leaderId) ->
                        request(ELECTED, new ElectedRequest(buildNode(leaderId)), leaderId));

        And("The node {long} becomes the leader node", (Long nodeId) -> assertLeader(nodeId));

        Then(
                "All nodes agree that node {long} is the leader node",
                (Long nodeId) -> nodeState.getNodes().forEach(node -> assertLeader(nodeId, node)));
    }

    private void verifyKeyIsDeletedAcrossTheCluster(String key) {
        nodesById.forEach(
                (id, __) ->
                        request(
                                GET,
                                new GetRequest(key),
                                id,
                                (((response, error) -> {
                                    assertThat("Node: %d".formatted(id), error, nullValue());
                                    assertThat(
                                            "Node: %d".formatted(id),
                                            ((GetResponse) response.payload()).value(),
                                            nullValue());
                                }))));
    }

    private Collection<Node> extractNodesFromDeregisterResponse(Response response) {
        return ((DeregisterResponse) response.payload()).nodes();
    }

    private Collection<Node> extractNodesFromRegisterResponse(Response response) {
        return ((RegisterResponse) response.payload()).nodes();
    }

    private Collection<Node> extractNodesFromListNodesResponse(Response response) {
        return ((ListNodesResponse) response.payload()).nodes();
    }

    private void addNodeToCluster(Integer port) {
        Long nodeId = port * 2L;
        request(
                REGISTER,
                new RegisterRequest("127.0.0.1", port, nodeId),
                defaultNodeId,
                defaultHandler.andThen(
                        (response, error) -> {
                            nodeState.setNodeId(nodeId);
                            nodeState.setNodes(extractNodesFromRegisterResponse(response));
                            assertThat(
                                    nodeState.getNodes().stream()
                                            .map(Node::uniqueId)
                                            .anyMatch(nodeId::equals),
                                    is(true));
                        }));
    }

    private void updateNodeState(Long nodeId) {
        request(
                DEREGISTER,
                new DeregisterRequest(nodeId),
                defaultNodeId,
                defaultHandler.andThen(
                        (response, error) -> {
                            nodeState.setNodeId(null);
                            nodeState.setNodes(extractNodesFromDeregisterResponse(response));
                        }));
    }

    private Long getLeaderNodeId() {
        return nodeState.getLeader().map(Node::uniqueId).orElse(defaultNodeId);
    }

    private void assertLeader(Long nodeId) {
        requestListOfNodes(
                defaultNodeId,
                defaultHandler.andThen(
                        (response, error) -> {
                            nodeState.setNodes(extractNodesFromListNodesResponse(response));
                            var leaderId = nodeState.getLeader().map(Node::uniqueId).orElse(null);
                            assertThat(leaderId, equalTo(nodeId));
                        }));
    }

    private void assertLeader(Long nodeId, Node node) {
        requestListOfNodes(
                node.uniqueId(),
                defaultHandler.andThen(
                        (response, error) -> {
                            var leaderId =
                                    getLeader(extractNodesFromListNodesResponse(response))
                                            .uniqueId();
                            assertThat(
                                    "Node %d disagrees".formatted(node.uniqueId()),
                                    leaderId,
                                    equalTo(nodeId));
                        }));
    }

    private void requestListOfNodes(Long nodeId, BiConsumer<Response, Throwable> handler) {
        request(LIST, null, nodeId, handler);
    }
}
