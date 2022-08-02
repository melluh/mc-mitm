package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import io.netty.buffer.ByteBuf;

public class ShortField extends PacketField {

    public ShortField(JsonObject json) {
        super(FieldType.SHORT, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        return buf.readShort();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeShort((Short) data);
    }

}
