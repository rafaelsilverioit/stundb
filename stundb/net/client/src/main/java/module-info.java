module stundb.net.client {
    requires transitive stundb.net.core;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.codec;
    requires java.security.sasl;
    requires stundb.core;
    requires com.google.common;
    requires io.netty.buffer;

    exports com.stundb.net.client;
    exports com.stundb.net.client.modules;
    exports com.stundb.net.client.modules.providers;
    exports com.stundb.net.client.handlers;

    opens com.stundb.net.client to
            com.google.guice;
    opens com.stundb.net.client.modules to
            com.google.guice;
    opens com.stundb.net.client.modules.providers to
            com.google.guice;
    opens com.stundb.net.client.handlers to
            com.google.guice;
}
