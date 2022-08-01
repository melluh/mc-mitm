package com.melluh.mccli;

import io.netty.buffer.ByteBuf;

public class ProtocolUtils {

    private ProtocolUtils() {}

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while(true) {
            currentByte = buf.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
            if(position >= 32)
                throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public static long readVarLong(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while(true) {
            currentByte = buf.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
            if(position >= 64)
                throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public static void writeVarInt(ByteBuf buf, int value) {

    }

    public static void writeVarLong(ByteBuf buf, long value) {

    }



}
