module stundb.net.core {
    requires fury.core;
    requires transitive stundb.api;

    exports com.stundb.net.core.codecs;
    exports com.stundb.net.core.codecs.fury;
    exports com.stundb.net.core.models;
    exports com.stundb.net.core.models.requests;
    exports com.stundb.net.core.models.responses;
    exports com.stundb.net.core.modules.providers;
}
