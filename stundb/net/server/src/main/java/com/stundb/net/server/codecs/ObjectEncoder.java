package com.stundb.net.server.codecs;

import com.stundb.core.logging.Loggable;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.models.responses.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@AllArgsConstructor
public class ObjectEncoder extends MessageToByteEncoder<Response> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Codec codec;

    @Loggable
    @Override
    protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) throws IOException {
        try {
            byte[] encode = codec.encode(msg);
            out.writeBytes(encode);
        } catch (RuntimeException e) {
            logger.error("Error encoding response", e);
        }
    }
}
