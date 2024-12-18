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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.internals.ScramExtensions. It has been heavily modified
 * for the needs of this project.
 */
public class ScramExtensions extends SaslExtensions {

    public ScramExtensions() {
        this(Collections.emptyMap());
    }

    public ScramExtensions(String extensions) {
        this(parseMap(extensions));
    }

    public ScramExtensions(Map<String, String> extensionMap) {
        super(extensionMap);
    }

    /**
     * Converts an extensions string into a {@code Map<String, String>}.
     *
     * <p>Example: {@code parseMap("key=hey,keyTwo=hi,keyThree=hello", "=", ",") => { key: "hey",
     * keyTwo: "hi", keyThree: "hello" }}
     */
    private static Map<String, String> parseMap(String mapStr) {
        return Arrays.stream(mapStr.split(","))
                .map(token -> token.split("=", 2))
                .collect(Collectors.toMap(data -> data[0], data -> data[1]));
    }
}
