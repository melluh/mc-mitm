package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class UnsignedByteField extends PacketField {

    public UnsignedByteField(JsonObject json) {
        super(FieldType.UBYTE, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return buf.readUnsignedByte();
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeByte((short) data);
    }

}
