package com.stundb.net.server.codecs;

import com.stundb.core.logging.Loggable;
import com.stundb.net.core.codecs.Codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ObjectDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
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
            logger.error("Error decoding request", e);
        }
    }
}
