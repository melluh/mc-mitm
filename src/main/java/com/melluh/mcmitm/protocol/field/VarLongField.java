package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class VarLongField extends PacketField {

    public VarLongField(JsonObject json) {
        super(FieldType.VARLONG, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return NetworkUtils.readVarLong(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        NetworkUtils.writeVarLong(buf, (long) data);
    }

}
