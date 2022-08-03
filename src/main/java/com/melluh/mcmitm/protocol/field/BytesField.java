package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class BytesField extends PacketField {

    public BytesField(JsonObject json) {
        super(FieldType.BYTES, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        int length = buf.readableBytes();
        byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }

    @Override
    public void write(ByteBuf buf, Object obj) {
        byte[] data = (byte[]) obj;
        buf.writeBytes(data);
    }

}
