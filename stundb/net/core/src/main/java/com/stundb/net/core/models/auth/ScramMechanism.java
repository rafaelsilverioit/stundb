/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stundb.net.core.models.auth;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.internals.ScramMechanism. It has been heavily
 * modified for the needs of this project.
 */
public enum ScramMechanism {
    SCRAM_SHA_256("SHA-256", "HmacSHA256", 4096, 16384);

    private static final Map<String, ScramMechanism> MECHANISMS_MAP;

    static {
        MECHANISMS_MAP =
                Arrays.stream(values())
                        .collect(
                                Collectors.toMap(
                                        mechanism -> mechanism.mechanismName, Function.identity()));
    }

    private final String mechanismName;
    private final String hashAlgorithm;
    private final String macAlgorithm;
    private final int minIterations;
    private final int maxIterations;

    ScramMechanism(
            String hashAlgorithm, String macAlgorithm, int minIterations, int maxIterations) {
        this.mechanismName = "SCRAM-" + hashAlgorithm;
        this.hashAlgorithm = hashAlgorithm;
        this.macAlgorithm = macAlgorithm;
        this.minIterations = minIterations;
        this.maxIterations = maxIterations;
    }

    public static ScramMechanism forMechanismName(String mechanismName) {
        return MECHANISMS_MAP.get(mechanismName);
    }

    public static Collection<String> mechanisms() {
        return MECHANISMS_MAP.keySet();
    }

    public final String mechanismName() {
        return mechanismName;
    }

    public String hashAlgorithm() {
        return hashAlgorithm;
    }

    public String macAlgorithm() {
        return macAlgorithm;
    }

    public int minIterations() {
        return minIterations;
    }

    public int maxIterations() {
        return maxIterations;
    }
}
