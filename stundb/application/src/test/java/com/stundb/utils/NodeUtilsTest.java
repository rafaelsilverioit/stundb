package com.stundb.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class NodeUtilsTest {

    private static final long NODE_UNIQUE_ID = 123456L;
    private static final long ANOTHER_NODE_UNIQUE_ID = 654321L;

    @Spy private MessageDigest digester = MessageDigest.getInstance("SHA-256");
    @InjectMocks private NodeUtils testee;

    public NodeUtilsTest() throws NoSuchAlgorithmException {}

    @Test
    void test_generateUniqueId() {
        var key = "0.0.0.0:8000";
        var uniqueId = testee.generateUniqueId(key);

        verify(digester).reset();
        verify(digester).update(key.getBytes());
        verify(digester).digest();

        assertEquals(uniqueId, 2390907759L);
    }

    @Test
    void test_filterNodesByState() {
        var node = aNode(NODE_UNIQUE_ID, NodeStatus.State.RUNNING);
        var anotherNode = aNode(ANOTHER_NODE_UNIQUE_ID, NodeStatus.State.RUNNING);
        var filtered =
                testee.filterNodesByState(
                                List.of(node, anotherNode),
                                NODE_UNIQUE_ID,
                                List.of(NodeStatus.State.RUNNING))
                        .toList();

        assertEquals(filtered.size(), 1);
        assertEquals(filtered.get(0), anotherNode);
    }

    @Test
    void test_filterNodesByState_when_other_nodes_are_failing() {
        var node = aNode(NODE_UNIQUE_ID, NodeStatus.State.RUNNING);
        var anotherNode = aNode(ANOTHER_NODE_UNIQUE_ID, NodeStatus.State.FAILING);
        var filtered =
                testee.filterNodesByState(
                                List.of(node, anotherNode),
                                NODE_UNIQUE_ID,
                                List.of(NodeStatus.State.RUNNING))
                        .toList();

        assertEquals(filtered.size(), 0);
    }

    @Test
    void test_filterNodesByState_when_no_other_nodes_are_found() {
        var node = aNode(NODE_UNIQUE_ID, NodeStatus.State.RUNNING);
        var filtered =
                testee.filterNodesByState(
                                List.of(node), NODE_UNIQUE_ID, List.of(NodeStatus.State.RUNNING))
                        .toList();

        assertEquals(filtered.size(), 0);
    }

    @Test
    void test_filterNodesByState_when_nodes_is_empty() {
        var filtered =
                testee.filterNodesByState(
                                List.of(), NODE_UNIQUE_ID, List.of(NodeStatus.State.RUNNING))
                        .toList();

        assertEquals(filtered.size(), 0);
    }

    private Node aNode(Long uniqueId, NodeStatus.State state) {
        return new Node("0.0.0.0", 8000, uniqueId, true, NodeStatus.create(state));
    }
}
