module stundb.net.core {
    requires fury.core;
    requires transitive stundb.api;
    requires stundb.core;
    requires jdk.security.auth;
    requires com.google.common;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires java.security.sasl;
    requires java.sql;
    requires com.fasterxml.jackson.dataformat.csv;

    exports com.stundb.net.core.codecs;
    exports com.stundb.net.core.codecs.fury;
    exports com.stundb.net.core.models;
    exports com.stundb.net.core.models.requests;
    exports com.stundb.net.core.models.responses;
    exports com.stundb.net.core.modules.providers;
    exports com.stundb.net.core.modules;
    exports com.stundb.net.core.security.auth.credentials;
    exports com.stundb.net.core.security.auth.callback;
    exports com.stundb.net.core.security.auth.callback.handlers;
    exports com.stundb.net.core.managers;
    exports com.stundb.net.core.managers.impl;
    exports com.stundb.net.core.models.auth;
    exports com.stundb.net.core.security.auth;

    opens com.stundb.net.core.modules.providers to
            com.google.guice;
    opens com.stundb.net.core.managers to
            com.google.guice;
    opens com.stundb.net.core.managers.impl to
            com.google.guice;
}
