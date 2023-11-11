package com.stundb.core.codecs.kryo;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class DefaultKryoContext implements KryoContext {

    private static final Integer defaultBufferSize = 1024 * 1024;

    private final KryoPool pool;

    private DefaultKryoContext(KryoClassRegistrator registrator) {
        var factory = new KryoFactoryImpl(registrator);
        this.pool = new KryoPool.Builder(factory)
                .softReferences()
                .build();
    }

    public static KryoContext newKryoContextFactory(KryoClassRegistrator registrator) {
        return new DefaultKryoContext(registrator);
    }

    @Override
    public byte[] encode(Object data) {
        return encode(data, defaultBufferSize);
    }

    @Override
    public byte[] encode(Object data, int bufferSize) {
        try {
            var output = new Output(new ByteArrayOutputStream(bufferSize));
            var kryo = pool.borrow();
            kryo.writeClassAndObject(output, data);
            var serialized = output.toBytes();
            output.close();
            pool.release(kryo);
            return serialized;
        } catch (KryoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object decode(byte[] serialized, Class<?> clazz) {
        try {
            var kryo = pool.borrow();
            var input = new Input(serialized);
            var obj = kryo.readClassAndObject(input);
            input.close();
            pool.release(kryo);
            return obj;
        } catch (KryoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object decode(InputStream serialized, Class<?> clazz) {
        try {
            var kryo = pool.borrow();
            var input = new Input(serialized, defaultBufferSize);
            var obj = kryo.readClassAndObject(input);
            input.close();
            pool.release(kryo);
            return obj;
        } catch (KryoException e) {
            throw new RuntimeException(e);
        }
    }
}
