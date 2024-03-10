module stundb.api {
    requires org.mapstruct;
    requires transitive lombok;
    requires transitive org.slf4j;
    requires transitive jakarta.inject;
    requires transitive org.yaml.snakeyaml;

    exports com.stundb.api.configuration;
    exports com.stundb.api.crdt;
    exports com.stundb.api.mappers;
    exports com.stundb.api.models;
    exports com.stundb.api.providers;

    opens com.stundb.api.configuration to com.google.guice;
    opens com.stundb.api.mappers to com.google.guice;
    opens com.stundb.api.models to com.google.guice;
    opens com.stundb.api.providers to com.google.guice;
}
