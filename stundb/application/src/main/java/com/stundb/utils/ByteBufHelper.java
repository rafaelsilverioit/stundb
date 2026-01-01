package com.stundb.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class ByteBufHelper {

    private static final Long MAX_BYTES_ALLOWED_IN_HEAP = 64_000L;

    private ByteBufHelper() {}

    public static ByteBuf of(Object value) {
        ByteBuf buffer = Unpooled.EMPTY_BUFFER;

        if (value == null) {
            return buffer;
        }

        if (value instanceof byte[] bytes) {
            // keep large objects off the heap to decrease number of garbage collections
            if (bytes.length > MAX_BYTES_ALLOWED_IN_HEAP) {
                buffer = PooledByteBufAllocator.DEFAULT.directBuffer(bytes.length);
                buffer.writeBytes(bytes);
                buffer.retain();
                return buffer;
            }
            return Unpooled.wrappedBuffer(bytes);
        }

        return buffer;
    }

    public static byte[] from(ByteBuf data) {
        byte[] bytes = null;
        if (data != null && data.hasArray()) {
            bytes = data.array();
        }
        return bytes;
    }

    public static void release(ByteBuf v) {
        if (v != null && v.readableBytes() > MAX_BYTES_ALLOWED_IN_HEAP) {
            v.release();
        }
    }
}
