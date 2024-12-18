module stundb.api {
    requires org.mapstruct;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.datatype.jsr310;
    requires transitive com.fasterxml.jackson.dataformat.yaml;
    requires org.hibernate.validator;
    requires transitive jakarta.validation;
    requires static transitive lombok;
    requires transitive org.slf4j;
    requires transitive jakarta.inject;

    exports com.stundb.api.btree;
    exports com.stundb.api.configuration;
    exports com.stundb.api.crdt;
    exports com.stundb.api.mappers;
    exports com.stundb.api.models;
    exports com.stundb.api.providers;

    opens com.stundb.api.btree to
            com.google.guice,
            com.fasterxml.jackson.databind;
    opens com.stundb.api.configuration to
            com.google.guice,
            com.fasterxml.jackson.databind,
            org.hibernate.validator;
    opens com.stundb.api.mappers to
            com.google.guice,
            com.fasterxml.jackson.databind,
            org.hibernate.validator;
    opens com.stundb.api.models to
            com.google.guice,
            com.fasterxml.jackson.databind,
            org.hibernate.validator;
    opens com.stundb.api.providers to
            com.google.guice,
            com.fasterxml.jackson.databind,
            org.hibernate.validator;
}
