package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class VarLongField extends PacketField {

    public VarLongField(JsonObject json) {
        super(FieldType.VARLONG, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        return NetworkUtils.readVarLong(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        NetworkUtils.writeVarLong(buf, (Long) data);
    }

}
