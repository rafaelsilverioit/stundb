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

import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.security.auth.ScramMessages.*;
import com.stundb.net.core.security.auth.callback.ScramExtensionsCallback;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.security.auth.callback.*;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.internals.ScramSaslClient. It has been heavily modified
 * for the needs of this project.
 *
 * <p>SaslClient implementation for SASL/SCRAM.
 *
 * <p>This implementation expects a login module that populates username as the Subject's public
 * credential and password as the private credential.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802">RFC 5802</a>
 */
@Slf4j
public class ScramSaslClient implements SaslClient {

    private final ScramMechanism mechanism;
    private final ScramFormatter formatter;
    private final CallbackHandler callbackHandler;
    private String clientNonce;
    private byte[] saltedPassword;
    private State state;
    private ClientFirstMessage clientFirstMessage;
    private ServerFirstMessage serverFirstMessage;
    private ClientFinalMessage clientFinalMessage;

    public ScramSaslClient(ScramMechanism mechanism, CallbackHandler callbackHandler)
            throws NoSuchAlgorithmException {
        this.mechanism = mechanism;
        this.callbackHandler = callbackHandler;
        this.formatter = new ScramFormatter(mechanism);
        setState(State.SEND_CLIENT_FIRST_MESSAGE);
    }

    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
        try {
            switch (state) {
                case SEND_CLIENT_FIRST_MESSAGE:
                    if (challenge != null && challenge.length != 0) {
                        throw new SaslException("Expected empty challenge");
                    }

                    clientNonce = formatter.secureRandomString();
                    var nameCallback = new NameCallback("username: ");
                    var extensionsCallback = new ScramExtensionsCallback();

                    try {
                        callbackHandler.handle(new Callback[] {nameCallback});
                        try {
                            callbackHandler.handle(new Callback[] {extensionsCallback});
                        } catch (UnsupportedCallbackException e) {
                            log.warn(
                                    "Extensions callback is not supported by client callback handler {}, no extensions will be added",
                                    callbackHandler);
                        }
                    } catch (Throwable e) {
                        throw new SaslException("Couldn't obtain username or extensions", e);
                    }

                    var username = nameCallback.getName();
                    var saslName = ScramFormatter.saslName(username);
                    var extensions = extensionsCallback.extensions();

                    clientFirstMessage = new ClientFirstMessage(saslName, clientNonce, extensions);
                    setState(State.RECEIVE_SERVER_FIRST_MESSAGE);

                    return clientFirstMessage.toBytes();
                case RECEIVE_SERVER_FIRST_MESSAGE:
                    serverFirstMessage = new ServerFirstMessage(challenge);

                    if (serverFirstMessage.error() != null) {
                        throw new SaslException(
                                "Sasl authentication using "
                                        + mechanism
                                        + " failed with error: "
                                        + serverFirstMessage.error());
                    }

                    if (!serverFirstMessage.nonce().startsWith(clientNonce)) {
                        throw new SaslException(
                                "Invalid server nonce: does not start with client nonce");
                    }

                    if (serverFirstMessage.iterations() < mechanism.minIterations()) {
                        throw new SaslException(
                                "Requested iterations "
                                        + serverFirstMessage.iterations()
                                        + " is less than the minimum "
                                        + mechanism.minIterations()
                                        + " for "
                                        + mechanism);
                    }

                    var passwordCallback = new PasswordCallback("password:", false);
                    try {
                        callbackHandler.handle(new Callback[] {passwordCallback});
                    } catch (Throwable e) {
                        throw new SaslException("Couldn't obtain password", e);
                    }

                    clientFinalMessage = handleServerFirstMessage(passwordCallback.getPassword());
                    setState(State.RECEIVE_SERVER_FINAL_MESSAGE);

                    return clientFinalMessage.toBytes();
                case RECEIVE_SERVER_FINAL_MESSAGE:
                    var serverFinalMessage = new ServerFinalMessage(challenge);

                    if (serverFinalMessage.error() != null) {
                        throw new SaslException(
                                "Sasl authentication using "
                                        + mechanism
                                        + " failed with error: "
                                        + serverFinalMessage.error());
                    }

                    handleServerFinalMessage(serverFinalMessage.serverSignature());
                    setState(State.COMPLETE);

                    return null;
                default:
                    throw new SaslException("Unexpected challenge in client state: " + state);
            }
        } catch (SaslException e) {
            setState(State.FAILED);
            throw e;
        }
    }

    @Override
    public String getMechanismName() {
        return mechanism.mechanismName();
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
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
        return null;
    }

    @Override
    public void dispose() {}

    private void setState(State state) {
        log.debug("Setting SASL/{} client state to {}", mechanism, state);
        this.state = state;
    }

    private ClientFinalMessage handleServerFirstMessage(char[] password) throws SaslException {
        try {
            var passwordBytes = ScramFormatter.normalize(new String(password));
            saltedPassword =
                    formatter.hi(
                            passwordBytes,
                            serverFirstMessage.salt(),
                            serverFirstMessage.iterations());

            var clientFinalMessage =
                    new ClientFinalMessage(
                            "n,,".getBytes(StandardCharsets.UTF_8), serverFirstMessage.nonce());

            var clientProof =
                    formatter.clientProof(
                            saltedPassword,
                            clientFirstMessage,
                            serverFirstMessage,
                            clientFinalMessage);
            clientFinalMessage.proof(clientProof);

            return clientFinalMessage;
        } catch (InvalidKeyException e) {
            throw new SaslException("Client final message could not be created", e);
        }
    }

    private void handleServerFinalMessage(byte[] signature) throws SaslException {
        try {
            var serverKey = formatter.serverKey(saltedPassword);
            var serverSignature =
                    formatter.serverSignature(
                            serverKey, clientFirstMessage, serverFirstMessage, clientFinalMessage);

            if (!MessageDigest.isEqual(signature, serverSignature)) {
                throw new SaslException("Invalid server signature in server final message");
            }
        } catch (InvalidKeyException e) {
            throw new SaslException("Sasl server signature verification failed", e);
        }
    }

    enum State {
        SEND_CLIENT_FIRST_MESSAGE,
        RECEIVE_SERVER_FIRST_MESSAGE,
        RECEIVE_SERVER_FINAL_MESSAGE,
        COMPLETE,
        FAILED
    }

    public static class ScramSaslClientFactory implements SaslClientFactory {

        @Override
        public SaslClient createSaslClient(
                String[] mechanisms,
                String authorizationId,
                String protocol,
                String serverName,
                Map<String, ?> props,
                CallbackHandler cbh)
                throws SaslException {

            var mechanism =
                    Arrays.stream(mechanisms)
                            .map(ScramMechanism::forMechanismName)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new SaslException(
                                                    String.format(
                                                            "Requested mechanisms '%s' not supported. Supported mechanisms are '%s'.",
                                                            Arrays.asList(mechanisms),
                                                            ScramMechanism.mechanisms())));

            try {
                return new ScramSaslClient(mechanism, cbh);
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException(
                        "Hash algorithm not supported for mechanism " + mechanism, e);
            }
        }

        @Override
        public String[] getMechanismNames(Map<String, ?> props) {
            Collection<String> mechanisms = ScramMechanism.mechanisms();
            return mechanisms.toArray(new String[0]);
        }
    }
}
