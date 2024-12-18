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
package com.stundb.net.core.security.auth;

import com.stundb.net.core.models.auth.ScramCredential;
import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.security.auth.ScramMessages.*;
import com.stundb.net.core.security.auth.callback.ScramCredentialCallback;

import lombok.extern.slf4j.Slf4j;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.security.auth.callback.*;
import javax.security.sasl.*;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.internals.ScramSaslServer. It has been heavily modified
 * for the needs of this project.
 *
 * <p>SaslServer implementation for SASL/SCRAM. This server is configured with a callback handler
 * for integration with a credential manager.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802">RFC 5802</a>
 */
@Slf4j
public class ScramSaslServer implements SaslServer {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of();

    private final ScramMechanism mechanism;
    private final ScramFormatter formatter;
    private final CallbackHandler callbackHandler;
    private State state;
    private ClientFirstMessage clientFirstMessage;
    private ServerFirstMessage serverFirstMessage;
    private ScramCredential scramCredential;
    private String authorizationId;

    public ScramSaslServer(ScramMechanism mechanism, CallbackHandler callbackHandler)
            throws NoSuchAlgorithmException {
        this.mechanism = mechanism;
        this.callbackHandler = callbackHandler;
        this.formatter = new ScramFormatter(mechanism);
        setState(State.RECEIVE_CLIENT_FIRST_MESSAGE);
    }

    @Override
    public byte[] evaluateResponse(byte[] response) throws SaslException {
        try {
            switch (state) {
                case RECEIVE_CLIENT_FIRST_MESSAGE:
                    clientFirstMessage = new ClientFirstMessage(response);
                    var scramExtensions = clientFirstMessage.extensions();

                    if (!SUPPORTED_EXTENSIONS.containsAll(scramExtensions.map().keySet())) {
                        log.warn(
                                "Unsupported extensions will be ignored, supported {}, provided {}",
                                SUPPORTED_EXTENSIONS,
                                scramExtensions.map().keySet());
                    }

                    var serverNonce = formatter.secureRandomString();

                    var saslName = clientFirstMessage.saslName();
                    var username = ScramFormatter.username(saslName);

                    var nameCallback = new NameCallback("username: ", username);
                    var credentialCallback = new ScramCredentialCallback();
                    try {
                        callbackHandler.handle(new Callback[] {nameCallback, credentialCallback});
                    } catch (Throwable e) {
                        throw new SaslException("other-error", e);
                    }

                    authorizationId = username;
                    scramCredential = credentialCallback.scramCredential();

                    if (scramCredential == null) {
                        throw new SaslException("authentication-failed");
                    }

                    if (scramCredential.iterations() < mechanism.minIterations()) {
                        log.warn(
                                "Iterations {} is less than the minimum {} for {}",
                                scramCredential.iterations(),
                                mechanism.minIterations(),
                                mechanism);
                        throw new SaslException("other-error");
                    }

                    serverFirstMessage =
                            new ServerFirstMessage(
                                    clientFirstMessage.nonce(),
                                    serverNonce,
                                    scramCredential.salt(),
                                    scramCredential.iterations());
                    setState(State.RECEIVE_CLIENT_FINAL_MESSAGE);

                    return serverFirstMessage.toBytes();
                case RECEIVE_CLIENT_FINAL_MESSAGE:
                    var clientFinalMessage = new ClientFinalMessage(response);

                    if (!clientFinalMessage.nonce().endsWith(serverFirstMessage.nonce())) {
                        throw new SaslException("invalid-encoding");
                    }

                    verifyClientProof(clientFinalMessage);

                    var serverKey = scramCredential.serverKey();
                    byte[] serverSignature;
                    try {
                        serverSignature =
                                formatter.serverSignature(
                                        serverKey,
                                        clientFirstMessage,
                                        serverFirstMessage,
                                        clientFinalMessage);
                    } catch (InvalidKeyException e) {
                        throw new SaslException("authentication-failed", e);
                    }
                    var serverFinalMessage = new ServerFinalMessage(null, serverSignature);

                    clearCredentials();
                    setState(State.COMPLETE);

                    return serverFinalMessage.toBytes();
                default:
                    throw new IllegalStateException("other-error");
            }
        } catch (SaslException e) {
            clearCredentials();
            setState(State.FAILED);
            throw e;
        }
    }

    @Override
    public String getAuthorizationID() {
        return authorizationId;
    }

    @Override
    public String getMechanismName() {
        return mechanism.mechanismName();
    }

    @Override
    public boolean isComplete() {
        return state == State.COMPLETE;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) {
        if (!isComplete()) {
            throw new IllegalStateException("Authentication exchange has not completed");
        }
        return Arrays.copyOfRange(incoming, offset, offset + len);
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) {
        if (!isComplete()) {
            throw new IllegalStateException("Authentication exchange has not completed");
        }
        return Arrays.copyOfRange(outgoing, offset, offset + len);
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        if (!isComplete()) {
            throw new IllegalStateException("Authentication exchange has not completed");
        }
        // TODO: look for an extension to handle session tokens
        return null;
    }

    @Override
    public void dispose() {
        clearCredentials();
    }

    private void verifyClientProof(ClientFinalMessage clientFinalMessage) throws SaslException {
        try {
            var expectedStoredKey = scramCredential.storedKey();
            var clientSignature =
                    formatter.clientSignature(
                            expectedStoredKey,
                            clientFirstMessage,
                            serverFirstMessage,
                            clientFinalMessage);
            var computedStoredKey =
                    formatter.storedKey(clientSignature, clientFinalMessage.proof());

            if (!MessageDigest.isEqual(computedStoredKey, expectedStoredKey)) {
                throw new SaslException("authentication-failed");
            }
        } catch (InvalidKeyException e) {
            throw new SaslException("other-error", e);
        }
    }

    private void setState(State state) {
        log.debug("Setting SASL/{} server state to {}", mechanism, state);
        this.state = state;
    }

    private void clearCredentials() {
        scramCredential = null;
        clientFirstMessage = null;
        serverFirstMessage = null;
    }

    enum State {
        RECEIVE_CLIENT_FIRST_MESSAGE,
        RECEIVE_CLIENT_FINAL_MESSAGE,
        COMPLETE,
        FAILED
    }

    @Slf4j
    public static class ScramSaslServerFactory implements SaslServerFactory {

        @Override
        public SaslServer createSaslServer(
                String mechanismName,
                String protocol,
                String serverName,
                Map<String, ?> __,
                CallbackHandler cbh)
                throws SaslException {
            var mechanism =
                    Optional.ofNullable(ScramMechanism.forMechanismName(mechanismName))
                            .orElseThrow(
                                    () -> {
                                        log.warn(
                                                "Requested mechanism '{}' is not supported. Supported mechanisms are '{}'.",
                                                mechanismName,
                                                ScramMechanism.mechanisms());
                                        return new SaslException("other-error");
                                    });

            try {
                return new ScramSaslServer(mechanism, cbh);
            } catch (NoSuchAlgorithmException e) {
                log.warn("Hash algorithm not supported for mechanism {}", mechanismName);
                throw new SaslException("other-error", e);
            }
        }

        @Override
        public String[] getMechanismNames(Map<String, ?> props) {
            Collection<String> mechanisms = ScramMechanism.mechanisms();
            return mechanisms.toArray(new String[0]);
        }
    }
}
