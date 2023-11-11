package com.stundb.net.server.codecs;

import com.stundb.core.codecs.Codec;
import com.stundb.core.logging.Loggable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class KryoObjectEncoder extends MessageToByteEncoder<Object> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Codec codec;

    @Loggable
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws RuntimeException {
        try {
            byte[] encode = codec.encode(msg, out.capacity());
            out.writeBytes(encode);
        } catch (RuntimeException e) {
            logger.error("Error encoding response", e.getCause());
        }
    }
}
