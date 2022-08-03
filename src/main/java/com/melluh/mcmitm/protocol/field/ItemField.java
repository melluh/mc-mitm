package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import com.melluh.mcmitm.util.ItemStack;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class ItemField extends PacketField {

    public ItemField(JsonObject json) {
        super(FieldType.ITEM, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) throws IOException {
        boolean present = buf.readBoolean();
        if(present) {
            int itemId = NetworkUtils.readVarInt(buf);
            byte itemCount = buf.readByte();
            CompoundTag tag = NetworkUtils.readNbt(buf);
            return new ItemStack(itemId, itemCount, tag);
        }

        return new ItemStack(0, (byte) 0, null);
    }

    @Override
    public void write(ByteBuf buf, Object data) throws IOException {
        ItemStack itemStack = (ItemStack) data;
        if(!itemStack.isAir()) {
            buf.writeBoolean(true);
            NetworkUtils.writeVarInt(buf, itemStack.itemId());
            buf.writeByte(itemStack.itemCount());
            NetworkUtils.writeNbt(buf, itemStack.tag());
        } else {
            buf.writeBoolean(false);
        }
    }

}
