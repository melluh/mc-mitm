package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

public class LongArrayField extends PacketField {

    public LongArrayField(JsonObject json) {
        super(FieldType.LONG_ARRAY, json);
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) {
        int length = NetworkUtils.readVarInt(buf);
        long[] data = new long[length];
        for(int i = 0; i < data.length; i++)
            data[i] = buf.readLong();
        return data;
    }

    @Override
    public void write(ByteBuf buf, Object obj) {
        long[] data = (long[]) obj;
        NetworkUtils.writeVarInt(buf, data.length);
        for(long val : data) {
            buf.writeLong(val);
        }
    }

}
