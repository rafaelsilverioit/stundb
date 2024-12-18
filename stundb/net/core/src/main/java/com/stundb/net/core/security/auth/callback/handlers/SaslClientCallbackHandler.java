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

import static java.util.Optional.ofNullable;

import com.stundb.net.core.models.auth.SaslExtensions;
import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.security.auth.callback.ScramExtensionsCallback;

import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.authenticator.SaslClientCallbackHandler. It may be heavily
 * modified for the needs of this project.
 *
 * <p>Default callback handler for Sasl clients. The callbacks required for the SASL mechanism
 * configured for the client should be supported by this callback handler. See <a
 * href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/sasl/sasl-refguide.html">Java
 * SASL API</a> for the list of SASL callback handlers required for each SASL mechanism.
 *
 * <p>For adding custom SASL extensions, a {@link SaslExtensions} may be added to the subject's
 * public credentials
 */
@AllArgsConstructor
public class SaslClientCallbackHandler implements AuthenticateCallbackHandler {

    private ScramMechanism mechanism;

    private Subject subject;

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback nc) {
                extractAttribute(subject, sub -> sub.getPublicCredentials(String.class))
                        .ifPresentOrElse(nc::setName, () -> nc.setName(nc.getDefaultName()));
                continue;
            } else if (callback instanceof PasswordCallback pc) {
                var errorMessage =
                        "Could not login: the client is being asked for a password, but the credentials"
                                + " manager is empty.";

                var password =
                        extractAttribute(subject, sub -> sub.getPrivateCredentials(String.class))
                                .map(String::toCharArray)
                                .orElseThrow(
                                        () ->
                                                new UnsupportedCallbackException(
                                                        callback, errorMessage));

                pc.setPassword(password);
                continue;
            } else if (callback instanceof ScramExtensionsCallback sec
                    && ScramMechanism.mechanisms().contains(mechanism.mechanismName())) {
                extractAttribute(subject, sub -> sub.getPublicCredentials(Map.class))
                        .ifPresentOrElse(sec::extensions, () -> sec.extensions(Map.of()));
                continue;
            }

            throw new UnsupportedCallbackException(
                    callback,
                    "Unrecognized SASL ClientCallback " + callback.getClass().getSimpleName());
        }
    }

    private <T> Optional<T> extractAttribute(Subject subject, Function<Subject, Set<T>> extractor) {
        return ofNullable(subject)
                .map(extractor)
                .filter(Predicate.not(Set::isEmpty))
                .map(Set::iterator)
                .map(Iterator::next);
    }
}
