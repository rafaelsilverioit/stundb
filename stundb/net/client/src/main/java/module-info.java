module stundb.net.client {
    requires com.google.guice;
    requires transitive stundb.net.core;

    exports com.stundb.net.client;
    exports com.stundb.net.client.modules;
    exports com.stundb.net.client.modules.providers;

    opens com.stundb.net.client to com.google.guice;
    opens com.stundb.net.client.modules to com.google.guice;
    opens com.stundb.net.client.modules.providers to com.google.guice;
}
