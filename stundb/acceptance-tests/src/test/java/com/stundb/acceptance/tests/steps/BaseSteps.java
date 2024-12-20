package com.stundb.acceptance.tests.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import static java.util.Optional.ofNullable;

import com.google.inject.Guice;
import com.stundb.acceptance.tests.modules.Module;
import com.stundb.api.models.Tuple;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.NodeStatus;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.responses.Response;
import com.stundb.net.core.security.auth.ScramSaslClientProvider;
import com.stundb.net.core.security.auth.ScramSaslServerProvider;

import io.cucumber.java8.En;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

public abstract class BaseSteps implements En {

    static {
        ScramSaslClientProvider.initialize();
        ScramSaslServerProvider.initialize();
    }

    protected final StunDBClient client;
    protected final Properties properties;
    protected final Map<Long, Tuple<String, Integer>> nodesById = new LinkedHashMap<>();
    protected final Long defaultNodeId = 66508730L;
    protected BiConsumer<Response, Throwable> defaultHandler =
            (response, error) -> {
                assertThat(error, nullValue());
                assertThat(response.status(), is(Status.OK));
            };

    protected BaseSteps() {
        var configUrl = getClass().getClassLoader().getResource("jaas.config");
        ofNullable(configUrl)
                .ifPresentOrElse(
                        url ->
                                System.setProperty(
                                        "java.security.auth.login.config", url.toExternalForm()),
                        () -> {
                            throw new RuntimeException("JAAS config file not found in resources!");
                        });

        var injector = Guice.createInjector(new Module());
        client = injector.getInstance(StunDBClient.class);
        properties = injector.getInstance(Properties.class);

        addNode("main");
        addNode("secondary");
    }

    protected Node buildNode(Long nodeId) {
        var tuple = nodesById.get(nodeId);
        return new Node(
                tuple.left(),
                tuple.right(),
                nodeId,
                true,
                NodeStatus.create(NodeStatus.State.RUNNING));
    }

    protected void request(Command command, Object payload, Long nodeId) {
        request(command, payload, nodeId, defaultHandler);
    }

    protected void request(
            Command command,
            Object payload,
            Long nodeId,
            BiConsumer<Response, Throwable> responseHandler) {
        ofNullable(nodesById.get(nodeId))
                .ifPresentOrElse(
                        node ->
                                client.requestAsync(command, payload, node.left(), node.right())
                                        .whenComplete(responseHandler)
                                        .join(),
                        () ->
                                responseHandler.accept(
                                        null,
                                        new IllegalArgumentException(
                                                "Invalid node identifier '%d'".formatted(nodeId))));
    }

    private void addNode(String nodeType) {
        var id = Long.parseLong(getProperty(nodeType, "node.id"));
        var ip = getProperty(nodeType, "node.host");
        var port = Integer.parseInt(getProperty(nodeType, "node.port"));

        nodesById.putIfAbsent(id, new Tuple<>(ip, port));
    }

    private String getProperty(String nodeType, String property) {
        return properties.getProperty("%s.%s".formatted(nodeType, property));
    }
}
