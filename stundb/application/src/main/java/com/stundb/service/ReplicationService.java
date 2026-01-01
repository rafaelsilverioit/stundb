package com.stundb.service;

import com.stundb.api.crdt.Entry;
import com.stundb.api.models.Tuple;

import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.Map;

public interface ReplicationService {

    void synchronize(Collection<Entry> added, Collection<Entry> removed);

    Tuple<Collection<Entry>, Collection<Entry>> generateStateSnapshot();

    void add(String key, ByteBuf value);

    void remove(String key);

    Tuple<Collection<Entry>, Collection<Entry>> verifySynchroneity(Map<String, Long> versionClock);

    Map<String, Long> generateVersionClock();
}
