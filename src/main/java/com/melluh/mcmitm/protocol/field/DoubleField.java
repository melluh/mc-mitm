package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import io.netty.buffer.ByteBuf;

public class DoubleField extends PacketField {

    public DoubleField(JsonObject json) {
        super(FieldType.DOUBLE, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        return buf.readDouble();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeDouble((Double) data);
    }

}
