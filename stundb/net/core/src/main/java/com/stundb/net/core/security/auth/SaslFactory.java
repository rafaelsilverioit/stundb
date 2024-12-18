package com.stundb.net.core.security.auth;

import com.stundb.net.core.models.auth.ScramMechanism;
import com.stundb.net.core.security.auth.callback.handlers.AuthenticateCallbackHandler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

@Slf4j
public class SaslFactory {

    public static SaslClient createSaslClient(
            String servicePrincipal,
            String host,
            ScramMechanism mechanism,
            Map<String, ?> configs,
            AuthenticateCallbackHandler callbackHandler)
            throws SaslException {
        log.debug(
                "Creating SaslClient: service={};serviceHostname={};mechs={}",
                servicePrincipal,
                host,
                "[" + mechanism.mechanismName() + "]");
        try {
            return Optional.ofNullable(
                            Sasl.createSaslClient(
                                    new String[] {mechanism.mechanismName()},
                                    null,
                                    servicePrincipal,
                                    host,
                                    configs,
                                    callbackHandler))
                    .orElseThrow(
                            () ->
                                    new SaslException(
                                            "Failed to create SaslClient with mechanism "
                                                    + mechanism));
        } catch (CompletionException e) {
            throw new SaslException(
                    "Failed to create SaslClient with mechanism " + mechanism, e.getCause());
        }
    }

    public static SaslServer createSaslServer(
            ScramMechanism mechanism,
            String host,
            Map<String, ?> configs,
            AuthenticateCallbackHandler callbackHandler)
            throws IOException {
        log.debug(
                "Creating SaslServer: serviceHostname={};mechs={}",
                host,
                "[" + mechanism.mechanismName() + "]");
        try {
            return Optional.ofNullable(
                            Sasl.createSaslServer(
                                    mechanism.mechanismName(),
                                    "StunDB",
                                    host,
                                    configs,
                                    callbackHandler))
                    .orElseThrow(
                            () ->
                                    new SaslException(
                                            "Server failed to create a SaslServer to interact with a client during session authentication with server mechanism "
                                                    + mechanism));
        } catch (CompletionException e) {
            throw new SaslException(
                    "Server failed to create a SaslServer to interact with a client during session authentication with server mechanism "
                            + mechanism,
                    e.getCause());
        }
    }
}
