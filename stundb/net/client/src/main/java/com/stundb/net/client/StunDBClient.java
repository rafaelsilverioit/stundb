package com.stundb.net.client;

import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.Response;

import java.util.concurrent.CompletableFuture;

public interface StunDBClient {

    CompletableFuture<Response> requestAsync(Request request, String ip, Integer port);

    CompletableFuture<Response> requestAsync(
            Command command, Object payload, String ip, Integer port);
}
