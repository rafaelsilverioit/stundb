package com.stundb.net.core.codecs.fury;

import com.stundb.net.core.codecs.Codec;

import lombok.AllArgsConstructor;

import org.apache.fury.ThreadSafeFury;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class FuryCodec implements Codec {

    private final ThreadSafeFury fury;

    @Override
    public byte[] encode(Object data) {
        return fury.serialize(data);
    }

    @Override
    public Object decode(byte[] data) throws IOException {
        return fury.deserialize(data);
    }

    @Override
    public Object decode(InputStream serialized) throws IOException {
        return fury.deserialize(serialized.readAllBytes());
    }
}
