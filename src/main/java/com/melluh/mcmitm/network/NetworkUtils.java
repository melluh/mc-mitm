package com.melluh.mcmitm.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    private NetworkUtils() {}

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = buf.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;

            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        while((value & 0xFFFFFF80) != 0) {
            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }

        buf.writeByte(value);
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

    public static void writeVarLong(ByteBuf buf, long value) {
        while((value & 0xFFFFFFFFFFFFFF80L) != 0) {
            buf.writeByte((int) (value & 0x7F) | 0x80);
            value >>>= 7;
        }

        buf.writeByte((int) value);
    }

    public static String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        String str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + length);
        return str;
    }

    public static void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

}
