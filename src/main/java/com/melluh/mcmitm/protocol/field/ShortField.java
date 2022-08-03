package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class ShortField extends PacketField {

    public ShortField(JsonObject json) {
        super(FieldType.SHORT, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readShort();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeShort((short) data);
    }

}
