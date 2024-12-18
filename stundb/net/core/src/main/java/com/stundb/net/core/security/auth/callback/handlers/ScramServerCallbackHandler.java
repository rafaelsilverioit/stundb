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
package com.stundb.net.core.security.auth.callback.handlers;

import com.stundb.net.core.security.auth.callback.ScramCredentialCallback;
import com.stundb.net.core.security.auth.credentials.CredentialManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.internals.ScramServerCallbackHandler. It has been heavily
 * modified for the needs of this project.
 */
@RequiredArgsConstructor
public class ScramServerCallbackHandler implements AuthenticateCallbackHandler {

    private final CredentialManager credentialManager;
    @Getter private String username;

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback nc) {
                username = nc.getDefaultName();
                continue;
            } else if (callback instanceof ScramCredentialCallback sc) {
                sc.scramCredential(credentialManager.get(username));
                continue;
            }
            throw new UnsupportedCallbackException(callback);
        }
    }
}
