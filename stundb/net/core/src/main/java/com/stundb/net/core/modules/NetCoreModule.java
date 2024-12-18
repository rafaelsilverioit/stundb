package com.stundb.net.core.modules;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.google.inject.AbstractModule;
import com.stundb.net.core.managers.RequestManager;
import com.stundb.net.core.managers.impl.RequestManagerImpl;
import com.stundb.net.core.managers.impl.SessionManagerImpl;
import com.stundb.net.core.modules.providers.CredentialManagerProvider;
import com.stundb.net.core.security.auth.credentials.CredentialManager;
import com.stundb.net.core.managers.SessionManager;

public class NetCoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CsvMapper.class)
                .annotatedWith(com.stundb.net.core.annotations.CsvMapper.class)
                .toInstance(
                        new CsvMapper().configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, true));
        bind(CredentialManager.class)
                .toProvider(CredentialManagerProvider.class)
                .asEagerSingleton();
        bind(SessionManager.class)
                .to(SessionManagerImpl.class)
                .asEagerSingleton();
        bind(RequestManager.class)
                .to(RequestManagerImpl.class)
                .asEagerSingleton();
    }
}
