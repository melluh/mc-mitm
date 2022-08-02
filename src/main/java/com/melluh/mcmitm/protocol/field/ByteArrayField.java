package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class ByteArrayField extends PacketField {

    public ByteArrayField(JsonObject json) {
        super(FieldType.BYTE_ARRAY, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        int length = NetworkUtils.readVarInt(buf);
        byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }

    @Override
    public void write(ByteBuf buf, Object obj) {
        byte[] data = (byte[]) obj;
        NetworkUtils.writeVarInt(buf, data.length);
        buf.writeBytes(data);
    }

}
