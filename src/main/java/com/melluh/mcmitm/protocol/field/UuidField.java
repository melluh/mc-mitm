package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class UuidField extends PacketField {

    public UuidField(JsonObject json) {
        super(FieldType.UUID, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return NetworkUtils.readUuid(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        NetworkUtils.writeUuid(buf, (UUID) data);
    }

}
