package com.stundb.acceptance.tests.steps;

import static com.stundb.net.core.models.Command.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.*;

import io.cucumber.java8.En;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class CacheSteps extends BaseSteps implements En {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BiConsumer<Response, Throwable> capacityResponseHandler =
            defaultHandler.andThen(
                    (response, error) -> {
                        var capacity = ((CapacityResponse) response.payload());
                        logger.info("Capacity: {}", capacity.capacity());
                        assertThat(capacity.capacity(), notNullValue());
                    });

    public CacheSteps() {
        super();

        Given(
                "We are able to retrieve the cache capacity",
                () -> request(CAPACITY, null, defaultNodeId, capacityResponseHandler));

        Given(
                "An entry for key {string} and value {string} is recorded",
                (String key, String value) ->
                        request(SET, new SetRequest(key, value, null), defaultNodeId));

        When("We clear the cache", () -> request(CLEAR, null, defaultNodeId));

        Then(
                "We are able to check that the current value for key {string} is {string}",
                (String key, String value) ->
                        request(
                                GET,
                                new GetRequest(key),
                                defaultNodeId,
                                getDataResponseHandler(value)));

        Then(
                "We are able to remove an existing record for the key {string}",
                (String key) ->
                        request(
                                DEL,
                                new DelRequest(key),
                                defaultNodeId,
                                deleteResponseHandler(key)));

        Then(
                "No records are found for the key {string}",
                (String key) ->
                        request(
                                EXISTS,
                                new ExistsRequest(key),
                                defaultNodeId,
                                existsResponseHandler(key)));

        And("The cache is not empty", () -> assertCacheState("Cache is empty", false));

        And("The cache is empty", () -> assertCacheState("Cache is not empty", true));
    }

    private BiConsumer<Response, Throwable> getDataResponseHandler(String value) {
        return defaultHandler.andThen(
                (response, error) -> {
                    var payload = (GetResponse) response.payload();
                    logger.info("key={}, value={}", payload.key(), payload.value());
                    assertThat(payload.value(), notNullValue());
                    assertThat((String) payload.value(), equalTo(value));
                });
    }

    private BiConsumer<Response, Throwable> existsResponseHandler(String key) {
        return defaultHandler.andThen(
                (response, error) -> {
                    assertThat(
                            "Key still exists in the cache",
                            ((ExistsResponse) response.payload()).exists(),
                            is(false));
                    logger.info("No records exist for key {}", key);
                });
    }

    private BiConsumer<Response, Throwable> deleteResponseHandler(String key) {
        return defaultHandler.andThen(
                (response, error) -> {
                    assertThat(
                            "Error removing key from cache",
                            Status.ERROR.equals(response.status()),
                            is(false));
                    logger.info("Removed key={}!", key);
                });
    }

    private void assertCacheState(String message, boolean value) {
        request(
                IS_EMPTY,
                null,
                defaultNodeId,
                (response, error) -> {
                    assertThat(error, nullValue());
                    assertThat(
                            message, ((IsEmptyResponse) response.payload()).isEmpty(), is(value));
                });
    }
}
