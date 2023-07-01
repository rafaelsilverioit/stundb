package com.stundb.service;

import com.stundb.cache.Cache;
import com.stundb.clients.GrpcRunner;
import com.stundb.logging.Loggable;
import com.stundb.models.UniqueId;
import com.stundb.observers.NoOpObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class NodesServiceImpl
        extends NodesServiceGrpc.NodesServiceImplBase
        implements Service {

    private final Logger logger = LoggerFactory.getLogger(NodesServiceImpl.class);

    private final Timer timer = new Timer();

    @Inject
    private GrpcRunner<RegisterRequest, RegisterResponse> runner;

    @Inject
    private Cache<Node> internalCache;

    @Inject
    private ReplicationService replicationService;

    @Inject
    @Named("electionService")
    private AsyncService election;

    @Inject
    @Named("coordinatorTimerTask")
    private TimerTask coordinatorTimerTask;

    @Inject
    private UniqueId uniqueId;

    public void init() {
        timer.scheduleAtFixedRate(coordinatorTimerTask, 10, 15 * 1000);
    }

    @Loggable
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        onCompleted(PingResponse.newBuilder().build(), responseObserver);
    }

    @Loggable
    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        internalCache.put(String.valueOf(request.getUniqueId()), Node.newBuilder()
                .setIp(request.getIp())
                .setPort(request.getPort())
                .setLeader(false)
                .setUniqueId(request.getUniqueId())
                .build());

        internalCache.getAll()
                .stream()
                .filter(Node::getLeader)
                .filter(node -> node.getUniqueId() != uniqueId.getNumber())
                .findFirst()
                .ifPresent(node -> runner.run(node.getIp(), node.getPort(), request, new NoOpObserver<>()));

        var response = RegisterResponse.newBuilder()
                .setStatus(true)
                .addAllNodes(internalCache.getAll())
                .setState(replicationService.generateCrdtRequest())
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void list(ListNodesRequest request, StreamObserver<ListNodesResponse> responseObserver) {
        var response = ListNodesResponse.newBuilder()
                .addAllNodes(internalCache.getAll())
                .build();
        onCompleted(response, responseObserver);
    }

    @Loggable
    @Override
    public void startElection(StartElectionRequest request, StreamObserver<StartElectionResponse> responseObserver) {
        election.run().thenRun(() -> {
            var response = StartElectionResponse.newBuilder().build();
            onCompleted(response, responseObserver);
        });
    }

    @Loggable
    @Override
    public void elected(ElectedRequest request, StreamObserver<ElectedResponse> responseObserver) {
        var node = request.getLeader();
        logger.info("{} became the leader", node.getUniqueId());
        internalCache.getAll()
                .parallelStream()
                .filter(n -> n.getLeader() && n.getUniqueId() != node.getUniqueId())
                .forEach(n -> internalCache.put(String.valueOf(n.getUniqueId()), Node.newBuilder(n).setLeader(false).build()));
        internalCache.put(String.valueOf(node.getUniqueId()), node);
        var response = ElectedResponse.newBuilder().build();
        onCompleted(response, responseObserver);
    }

    @Override
    public void synchronize(CRDTRequest request, StreamObserver<CRDTResponse> responseObserver) {
        replicationService.synchronize(request);

        var response = CRDTResponse.newBuilder().setStatus(true).build();
        onCompleted(response, responseObserver);
    }
}
