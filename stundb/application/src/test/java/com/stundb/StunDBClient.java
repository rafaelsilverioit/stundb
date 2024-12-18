package com.stundb;

import com.google.inject.Guice;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.responses.RegisterResponse;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class StunDBClient {

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        var guice = Guice.createInjector(new ClientModule());
        var executor = guice.getInstance(ExecutorService.class);
        var client = guice.getInstance(com.stundb.net.client.StunDBClient.class);

        for (int i = 0; i < 2; i++) {
            var uuid = UUID.randomUUID() + "|" + i;
            var port = 8000 + i;

            client.requestAsync(
                            Command.REGISTER,
                            new RegisterRequest(uuid, 9999, 9999L),
                            "127.0.0.1",
                            port)
                    .handle(
                            (res, err) -> {
                                if (err != null) {
                                    err.printStackTrace();
                                    System.exit(1);
                                }
                                var payload = (RegisterResponse) res.payload();
                                System.out.println(payload.state().added());
                                System.out.println(payload.state().added().size());
                                return res;
                            });
        }
        executor.shutdown();
    }
}
