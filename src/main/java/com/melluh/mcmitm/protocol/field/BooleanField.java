package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class BooleanField extends PacketField {

    public BooleanField(JsonObject json) {
        super(FieldType.BOOLEAN, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readBoolean();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeBoolean((boolean) data);
    }

}
