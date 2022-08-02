package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class StringField extends PacketField {

    public StringField(JsonObject json) {
        super(FieldType.STRING, json);
    }

    @Override
    public Object read(ByteBuf buf) {
        return NetworkUtils.readString(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        NetworkUtils.writeString(buf, (String) data);
    }

}
