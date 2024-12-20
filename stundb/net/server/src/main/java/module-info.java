module stundb.net.server {
    requires io.netty.all;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.common;
    requires io.netty.handler;
    requires transitive io.netty.transport;
    requires stundb.core;
    requires transitive stundb.net.client;
    requires java.security.sasl;
    requires jdk.security.auth;
    requires com.google.common;

    exports com.stundb.net.server;
    exports com.stundb.net.server.handlers;

    opens com.stundb.net.server to
            com.google.guice;
    opens com.stundb.net.server.handlers to
            com.google.guice;
}
