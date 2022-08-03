package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class IntField extends PacketField {

    public IntField(JsonObject json) {
        super(FieldType.INT, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readInt();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeInt((int) data);
    }

}
