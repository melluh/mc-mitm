package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import com.melluh.mcmitm.util.BlockPos;
import io.netty.buffer.ByteBuf;

public class BlockPosField extends PacketField {

    public BlockPosField(JsonObject json) {
        super(FieldType.BLOCKPOS, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        return BlockPos.of(buf.readLong());
    }

    @Override
    public void write(ByteBuf buf, Object data) {
        buf.writeLong(((BlockPos) data).asLong());
    }

}
