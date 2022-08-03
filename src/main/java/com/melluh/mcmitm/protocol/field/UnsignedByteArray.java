package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class UnsignedByteArray extends PacketField {

    public UnsignedByteArray(JsonObject json) {
        super(FieldType.UNSIGNED_BYTE_ARRAY, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        int length = NetworkUtils.readVarInt(buf);
        short[] data = new short[length];
        for(int i = 0; i < length; i++)
            data[i] = buf.readUnsignedByte();
        return data;
    }

    @Override
    public void write(ByteBuf buf, Object obj) {
        byte[] data = (byte[]) obj;
        NetworkUtils.writeVarInt(buf, data.length);
        buf.writeBytes(data);
    }

}
