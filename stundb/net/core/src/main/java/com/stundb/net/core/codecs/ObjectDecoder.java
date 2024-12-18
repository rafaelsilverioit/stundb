package com.stundb.net.core.codecs;

import com.stundb.core.logging.Loggable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ObjectDecoder extends ByteToMessageDecoder {

    private final Codec codec;

    public ObjectDecoder(Codec codec) {
        setSingleDecode(false);
        this.codec = codec;
    }

    @Loggable
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws IOException {
        if (in == null || in.readableBytes() <= 0) {
            return;
        }

        var bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        try {
            var obj = codec.decode(bytes);
            if (obj != null) {
                out.add(obj);
            }
        } catch (RuntimeException e) {
            log.error("Error decoding request", e);
        }
    }
}
