package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import io.netty.buffer.ByteBuf;

public class FloatField extends PacketField {

    public FloatField(JsonObject json) {
        super(FieldType.FLOAT, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        return buf.readFloat();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeFloat((Float) data);
    }

}
