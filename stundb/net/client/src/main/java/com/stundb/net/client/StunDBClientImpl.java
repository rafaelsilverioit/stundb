package com.stundb.net.client;

import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class StunDBClientImpl implements StunDBClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private ExecutorService executor;

    @Inject private Codec codec;

    @Override
    public CompletableFuture<Response> requestAsync(Request request, String ip, Integer port) {
        return CompletableFuture.supplyAsync(
                () -> {
                    var start = System.currentTimeMillis();
                    try (var socket = new Socket()) {
                        return execute(request, ip, port, socket);
                    } catch (Exception e) {
                        logger.error("Request failed " + request, e);
                        throw new RuntimeException(e);
                    } finally {
                        logger.debug("Request took " + (System.currentTimeMillis() - start) + "ms");
                    }
                },
                executor);
    }

    private Response execute(Request request, String ip, Integer port, Socket socket)
            throws IOException {
        socket.connect(new InetSocketAddress(ip, port));
        var bytes = codec.encode(request);
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
        if (socket.isConnected()) {
            return (Response) codec.decode(socket.getInputStream());
        }
        throw new RuntimeException("Connection lost");
    }
}
