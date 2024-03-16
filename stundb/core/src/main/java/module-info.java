module stundb.core {
    requires aopalliance;
    requires transitive stundb.api;

    exports com.stundb.core.cache;
    exports com.stundb.core.crdt;
    exports com.stundb.core.logging;
    exports com.stundb.core.models;

    opens com.stundb.core.cache to
            com.google.guice;
}
