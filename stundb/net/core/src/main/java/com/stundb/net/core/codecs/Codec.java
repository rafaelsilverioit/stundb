package com.stundb.net.core.codecs;

import java.io.IOException;
import java.io.InputStream;

public interface Codec {

    byte[] encode(Object data);

    Object decode(byte[] data) throws IOException;

    Object decode(InputStream serialized) throws IOException;
}
