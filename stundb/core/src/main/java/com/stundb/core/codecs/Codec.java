package com.stundb.core.codecs;

import java.io.IOException;
import java.io.InputStream;

public interface Codec {

    byte[] encode(Object data);

    byte[] encode(Object data, int bufferSize) throws RuntimeException;

    Object decode(byte[] data, Class<?> clazz) throws RuntimeException;

    Object decode(InputStream serialized, Class<?> clazz) throws RuntimeException;
}
