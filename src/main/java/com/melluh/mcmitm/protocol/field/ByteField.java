package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class ByteField extends PacketField {

    public ByteField(JsonObject json) {
        super(FieldType.BYTE, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readByte();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeByte((byte) data);
    }

}
