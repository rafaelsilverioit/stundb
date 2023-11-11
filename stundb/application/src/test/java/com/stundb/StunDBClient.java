package com.stundb;

import com.google.inject.Guice;
import com.stundb.net.client.modules.ClientModule;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.requests.SetRequest;
import com.stundb.modules.Module;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class StunDBClient {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("img.jpeg");

        var guice = Guice.createInjector(new Module(), new ClientModule());
        var executor = guice.getInstance(ExecutorService.class);
        var client = guice.getInstance(com.stundb.net.client.StunDBClient.class);
        assert is != null;
        client.requestAsync(
                Request.buildRequest(Command.START_ELECTION, new SetRequest("rafael", "Base64.getEncoder().encodeToString(ByteStreams.toByteArray(is))", -1)),
                "127.0.0.1",
                8000)
                .handle((res, err) -> {
                    System.out.println("Replied");
                    if (err != null) {
                        err.printStackTrace();
                        System.exit(1);
                    }
                    //((ListNodesResponse)res.payload()).nodes().forEach(System.out::println);
                    return res;
                })
                .get();

//        client.requestAsync(
//                        Request.buildRequest(Command.GET, new GetRequest("rafael")),
//                        "127.0.0.1",
//                        9001)
//                .handle((res, err) -> {
//                    System.out.println("Replied2");
//                    if (err != null) {
//                        err.printStackTrace();
//                        System.exit(1);
//                    }
//
//                    var str = (GetResponse) res.payload();
//                    var data = ((String) str.value());
//
//                    //var file = new File("src/test/resources/output2.txt");
//                    System.out.println("Replied3");
////                    try(var inputStream = new ByteArrayInputStream(data)) {
////                        System.out.println("Replied4");
////                        //Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);
////                    } catch (IOException e) {
////                        System.out.println("Replied5.1");
////                        throw new RuntimeException(e);
////                    }
//                    System.out.println(data);
//                    return res;
//                })
//                .get();

        executor.shutdown();
//
//        var response = stub.get(GetRequest.newBuilder()
//                .setKey("realngnx")
//                .build());
//

//
//        channel.shutdown();
    }
}
