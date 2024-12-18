package com.stundb.net.core.codecs;

import com.stundb.core.logging.Loggable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ObjectEncoder extends MessageToByteEncoder<Object> {

    private final Codec codec;

    @Loggable
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        try {
            byte[] encode = codec.encode(msg);
            out.writeBytes(encode);
        } catch (RuntimeException e) {
            log.error("Error encoding response", e);
        }
    }
}
