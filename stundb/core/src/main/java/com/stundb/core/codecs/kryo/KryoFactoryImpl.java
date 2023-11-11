package com.stundb.core.codecs.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KryoFactoryImpl implements KryoFactory {

    private KryoClassRegistrator registrator;

    @Override
    public Kryo create() {
        var kryo = new Kryo();
        registrator.register(kryo);
        return kryo;
    }
}
