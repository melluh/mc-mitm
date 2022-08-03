package com.melluh.mcmitm.network;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    private static final Nbt NBT = new Nbt();

    public static CompoundTag readNbt(ByteBuf buf) throws IOException {
        if(buf.readByte() == 0)
            return new CompoundTag();
        buf.readerIndex(buf.readerIndex() - 1);
        DataInputStream inputStream = new DataInputStream(new ByteBufInputStream(buf));
        return NBT.fromStream(inputStream);
    }

    public static void writeNbt(ByteBuf buf, CompoundTag tag) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(new ByteBufOutputStream(buf));
        NBT.toStream(tag, outputStream);
    }

}
