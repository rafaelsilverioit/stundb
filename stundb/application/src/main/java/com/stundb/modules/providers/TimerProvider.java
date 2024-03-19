package com.stundb.modules.providers;

import jakarta.inject.Provider;

import java.util.Timer;

public class TimerProvider implements Provider<Timer> {

    @Override
    public Timer get() {
        return new Timer();
    }
}
