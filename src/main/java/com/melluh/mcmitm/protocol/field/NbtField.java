package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class NbtField extends PacketField {

    public NbtField(JsonObject json) {
        super(FieldType.NBT, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) throws IOException {
        return NetworkUtils.readNbt(buf);
    }

    @Override
    public void write(ByteBuf buf, Object data) throws IOException {
        NetworkUtils.writeNbt(buf, (CompoundTag) data);
    }

}
