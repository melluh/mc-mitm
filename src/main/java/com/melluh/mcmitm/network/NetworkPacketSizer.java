package com.melluh.mcmitm.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class NetworkPacketSizer extends ByteToMessageCodec<ByteBuf> {

    private static final int LENGTH_SIZE = 5;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        int length = in.readableBytes();
        out.ensureWritable(LENGTH_SIZE + length);
        NetworkUtils.writeVarInt(out, length);
        out.writeBytes(in);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if(buf.readableBytes() < LENGTH_SIZE)
            return; // need to have at least 5 bytes readable as that's the max size of a varint

        int length = NetworkUtils.readVarInt(buf);
        out.add(buf.readBytes(length));
    }

}
