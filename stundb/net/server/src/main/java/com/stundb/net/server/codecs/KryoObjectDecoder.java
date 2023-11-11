package com.stundb.net.server.codecs;

import com.stundb.core.codecs.Codec;
import com.stundb.core.logging.Loggable;
import com.stundb.net.core.models.requests.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KryoObjectDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Codec codec;

    public KryoObjectDecoder(Codec codec) {
        setSingleDecode(false);
        this.codec = codec;
    }

    @Loggable
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in == null || in.readableBytes() <= 0) {
            return;
        }

        var bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        try {
            var obj = codec.decode(bytes, Request.class);
            if (obj != null) {
                out.add(obj);
            }
        } catch (RuntimeException e) {
            logger.error("Error decoding request", e.getCause());
        }
    }
}
