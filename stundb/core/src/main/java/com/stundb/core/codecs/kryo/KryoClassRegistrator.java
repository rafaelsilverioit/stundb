package com.stundb.core.codecs.kryo;

import com.esotericsoftware.kryo.Kryo;

@FunctionalInterface
public interface KryoClassRegistrator {

    void register(Kryo kryo);
}
