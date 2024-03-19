package com.stundb.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Executor;
import com.stundb.api.models.Executors;
import com.stundb.core.cache.Cache;
import com.stundb.core.models.UniqueId;
import com.stundb.net.client.StunDBClient;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.CRDTRequest;
import com.stundb.net.core.models.requests.RegisterRequest;
import com.stundb.net.core.models.requests.Request;
import com.stundb.net.core.models.responses.ErrorResponse;
import com.stundb.net.core.models.responses.RegisterResponse;
import com.stundb.net.core.models.responses.Response;
import com.stundb.service.ReplicationService;
import com.stundb.timers.impl.BackoffTimerTaskImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeedServiceImplTest {

    public static final String LOCALHOST = "127.0.0.1";
    public static final String SEED_ADDRESS = "127.0.0.1:8080";
    private final Request request =
            Request.buildRequest(Command.REGISTER, new RegisterRequest(LOCALHOST, 9090, 1010L));
    private final Function<Object, Response> response =
            (payload) -> Response.buildResponse(request, Status.OK, payload);
    @Mock private StunDBClient client;
    @Mock private ApplicationConfig config;
    @Mock private Cache<Node> internalCache;
    @Mock private ReplicationService replicationService;
    @Mock private UniqueId uniqueId;
    @Mock private Timer timer;
    @InjectMocks private SeedServiceImpl testee;

    private static Stream<Arguments>
            contactSeeds_should_handle_successful_response_and_call_replication_service() {
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(new Node("", 0, 1234L, false, null))));
    }

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        var field = getField();
        when(config.executors()).thenReturn(new Executors(null, null, null, null, new Executor(1)));
        when(config.backoffSettings()).thenReturn(Map.of());
        field.set(testee, new BackoffTimerTaskImpl(timer, config));
    }

    @Test
    void contactSeeds_should_do_nothing_when_seeds_list_is_empty() {
        when(config.seeds()).thenReturn(List.of());

        testee.contactSeeds();

        verify(config, never()).ip();
        verify(config, never()).port();
        verify(config, times(2)).backoffSettings();
        verify(uniqueId, never()).number();
        verify(internalCache, never()).put(any(), any());
        verify(replicationService, never()).synchronize(any());
        verify(timer, times(1)).schedule(any(TimerTask.class), any(Long.class));
    }

    @ParameterizedTest
    @CsvSource({"8080", "9090"})
    void contactSeeds_should_work_successfully(Integer port) {
        when(config.seeds()).thenReturn(List.of(SEED_ADDRESS));
        when(config.ip()).thenReturn(LOCALHOST);
        when(config.port()).thenReturn(port);

        testee.contactSeeds();

        verify(config, times(1)).ip();
        verify(config, times(1)).port();
        verify(config, times(2)).backoffSettings();
        verify(uniqueId, never()).number();
        verify(internalCache, never()).put(any(), any());
        verify(replicationService, never()).synchronize(any());
        verify(timer, times(1)).schedule(any(TimerTask.class), any(Long.class));
    }

    @Test
    void contactSeeds_should_fail_when_invalid_seed_address_is_provided() {
        mockContactSeed("127.0.0.1-8080");

        var exception = assertThrows(InvocationTargetException.class, () -> testee.contactSeeds());
        assertEquals("Invalid seed address - 127.0.0.1-8080", exception.getCause().getMessage());

        verify(client, never()).requestAsync(any(), any(), any());
        verify(config, times(2)).backoffSettings();
        verify(uniqueId, never()).number();
        verify(internalCache, never()).put(any(), any());
        verify(replicationService, never()).synchronize(any());
    }

    @Test
    void contactSeeds_should_handle_failed_seed_response() {
        setupMocks(
                Response.buildResponse(
                        request, Status.ERROR, new ErrorResponse("error.invalid.command")));

        var exception = assertThrows(InvocationTargetException.class, () -> testee.contactSeeds());
        assertEquals(
                "Reply from seed was - error.invalid.command", exception.getCause().getMessage());

        verifyCalls(never(), never());
    }

    @ParameterizedTest
    @MethodSource("contactSeeds_should_handle_successful_response_and_call_replication_service")
    void contactSeeds_should_handle_successful_response_and_call_replication_service(
            List<Node> nodes) {
        setupMocks(response.apply(new RegisterResponse(nodes, new CRDTRequest(null, null))));

        testee.contactSeeds();

        verifyCalls(times(nodes.size()), times(1));
    }

    private void setupMocks(Response response) {
        mockContactSeed(SEED_ADDRESS);

        when(config.ip()).thenReturn(LOCALHOST);
        when(config.port()).thenReturn(9090);
        when(uniqueId.number()).thenReturn(1010L);
        when(client.requestAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(response));
    }

    private void verifyCalls(VerificationMode nodes, VerificationMode times) {
        verify(client, times(1)).requestAsync(any(), any(), any());
        verify(config, times(1)).ip();
        verify(config, times(1)).port();
        verify(config, times(2)).backoffSettings();
        verify(uniqueId, times(1)).number();
        verify(internalCache, nodes).put(any(), any());
        verify(replicationService, times).synchronize(any());
    }

    private void mockContactSeed(String seed) {
        Mockito.doAnswer(
                        (__) -> {
                            var method = getMethod(String.class);
                            method.invoke(testee, seed);
                            return null;
                        })
                .when(timer)
                .schedule(any(TimerTask.class), any(Long.class));
    }

    private Field getField() throws NoSuchFieldException {
        var field = testee.getClass().getDeclaredField("backoffTimerTask");
        field.setAccessible(true);
        return field;
    }

    @SafeVarargs
    private <T> Method getMethod(Class<T>... params) throws NoSuchMethodException {
        var method = testee.getClass().getDeclaredMethod("contactSeed", params);
        method.setAccessible(true);
        return method;
    }
}
