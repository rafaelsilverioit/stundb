module stundb.application {
    requires java.sql;
    requires com.google.guice;
    requires aopalliance;
    requires stundb.core;
    requires stundb.net.server;

    exports com.stundb;

    opens com.stundb.modules to com.google.guice;
    opens com.stundb.modules.providers to com.google.guice;
    opens com.stundb.utils to com.google.guice;
    opens com.stundb.server to com.google.guice;
    opens com.stundb.server.handlers.nodes to com.google.guice;
    opens com.stundb.server.handlers.store to com.google.guice;
    opens com.stundb.service.impl to com.google.guice;
    opens com.stundb.timers to com.google.guice;

    uses com.stundb.net.server.handlers.CommandHandler;
    uses com.stundb.net.server.handlers.RequestHandler;
    uses com.stundb.net.server.handlers.DefaultCommandHandler;

    provides com.stundb.net.server.handlers.CommandHandler with
        com.stundb.server.handlers.store.GetHandler,
        com.stundb.server.handlers.store.SetHandler,
        com.stundb.server.handlers.store.DelHandler,
        com.stundb.server.handlers.store.ClearHandler,
        com.stundb.server.handlers.store.CapacityHandler,
        com.stundb.server.handlers.store.ExistsHandler,
        com.stundb.server.handlers.store.IsEmptyHandler,
        com.stundb.server.handlers.nodes.ListHandler,
        com.stundb.server.handlers.nodes.RegisterHandler,
        com.stundb.server.handlers.nodes.DeregisterHandler,
        com.stundb.server.handlers.nodes.StartElectionHandler,
        com.stundb.server.handlers.nodes.ElectedHandler,
        com.stundb.server.handlers.nodes.SynchronizeHandler;
}
