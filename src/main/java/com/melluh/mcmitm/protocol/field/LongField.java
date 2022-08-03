package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class LongField extends PacketField {

    public LongField(JsonObject json) {
        super(FieldType.LONG, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readLong();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeLong((long) data);
    }

}
