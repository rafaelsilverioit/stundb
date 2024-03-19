open module acceptance.tests {
    requires com.google.guice;
    requires io.cucumber.core;
    requires io.cucumber.java8;
    requires io.cucumber.guice;
    requires org.hamcrest;
    requires org.junit.platform.suite.api;
    requires stundb.application;
    requires stundb.net.client;
    requires stundb.net.server;
    requires stundb.api;

    exports com.stundb.acceptance.tests to com.google.guice;
    exports com.stundb.acceptance.tests.modules to com.google.guice;
    exports com.stundb.acceptance.tests.modules.providers to com.google.guice;
    exports com.stundb.acceptance.tests.steps to com.google.guice;
}
