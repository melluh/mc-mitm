package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class VarIntField extends PacketField {

    public VarIntField(JsonObject json) {
        super(FieldType.VARINT, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return NetworkUtils.readVarInt(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        NetworkUtils.writeVarInt(buf, (int) data);
    }

}
