module stundb.core {
    requires aopalliance;
    requires transitive stundb.api;
    requires transitive com.google.guice;
    requires annotations;

    exports com.stundb.core.cache;
    exports com.stundb.core.crdt;
    exports com.stundb.core.logging;
    exports com.stundb.core.models;

    opens com.stundb.core.cache to
            com.google.guice;
}
