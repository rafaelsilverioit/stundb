package com.stundb.steps;

import static com.stundb.net.core.models.Command.*;

import com.stundb.net.core.models.Status;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.CapacityResponse;
import com.stundb.net.core.models.responses.ExistsResponse;
import com.stundb.net.core.models.responses.GetResponse;
import com.stundb.net.core.models.responses.IsEmptyResponse;

import io.cucumber.java8.En;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheSteps extends BaseSteps implements En {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    public CacheSteps() {
        super();

        Given(
                "We are able to retrieve the cache capacity",
                () -> {
                    client.requestAsync(Request.buildRequest(CAPACITY, null), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                        }
                                        var capacity = ((CapacityResponse) response.payload());
                                        logger.info("Capacity: {}", capacity.capacity());
                                    })
                            .join();
                });

        Given(
                "An entry for key {string} and value {string} is recorded",
                (String key, String value) -> {
                    client.requestAsync(
                                    Request.buildRequest(SET, new SetRequest(key, value, -1)),
                                    host,
                                    port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                        }
                                    })
                            .join();
                });

        When(
                "We clear the cache",
                () -> {
                    client.requestAsync(Request.buildRequest(CLEAR, null), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                        }
                                    })
                            .join();
                });

        Then(
                "We are able to retrieve the current value for the key {string}",
                (String key) -> {
                    client.requestAsync(Request.buildRequest(GET, new GetRequest(key)), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                            return;
                                        }
                                        var payload = (GetResponse) response.payload();
                                        logger.info("key={}, value={}", key, payload.value());
                                    })
                            .join();
                });

        Then(
                "We are able to remove an existing record for the key {string}",
                (String key) -> {
                    client.requestAsync(Request.buildRequest(DEL, new DelRequest(key)), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                            return;
                                        } else if (Status.ERROR.equals(response.status())) {
                                            throw new RuntimeException(
                                                    "Error removing key from cache");
                                        }

                                        logger.info("Removed key={}!", key);
                                    })
                            .join();
                });

        Then(
                "No records are found for the key {string}",
                (String key) -> {
                    client.requestAsync(
                                    Request.buildRequest(EXISTS, new ExistsRequest(key)),
                                    host,
                                    port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                            return;
                                        } else if (((ExistsResponse) response.payload()).exists()) {
                                            throw new RuntimeException(
                                                    "Key still exists in the cache");
                                        }
                                        logger.info("No records exist for key {}", key);
                                    })
                            .join();
                });

        And(
                "The cache is not empty",
                () -> {
                    client.requestAsync(Request.buildRequest(IS_EMPTY, null), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                        } else if (((IsEmptyResponse) response.payload())
                                                .isEmpty()) {
                                            throw new RuntimeException("Cache is empty");
                                        }
                                    })
                            .join();
                });

        And(
                "The cache is empty",
                () -> {
                    client.requestAsync(Request.buildRequest(IS_EMPTY, null), host, port)
                            .whenComplete(
                                    (response, error) -> {
                                        if (error != null) {
                                            logger.error("Something went wrong", error);
                                        } else if (!((IsEmptyResponse) response.payload())
                                                .isEmpty()) {
                                            throw new RuntimeException("Cache is not empty");
                                        }
                                    })
                            .join();
                });
    }
}
