package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkUtils;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectionField extends PacketField {

    private final PacketFieldList fieldList;

    public CollectionField(JsonObject json) {
        super(FieldType.COLLECTION, json);
        this.fieldList = PacketFieldList.create(json.getArray("fields"));
    }

    @Override
    public Object read(ByteBuf buf, PacketData parentData) throws IOException  {
        List<PacketData> dataList = new ArrayList<>();

        int length = NetworkUtils.readVarInt(buf);
        for(int i = 0; i < length; i++) {
            PacketData data = new PacketData(fieldList, parentData);
            data.read(buf);
            dataList.add(data);
        }

        return dataList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ByteBuf buf, Object obj) throws IOException {
        List<PacketData> dataList = (List<PacketData>) obj;
        NetworkUtils.writeVarInt(buf, dataList.size());
        for (PacketData data : dataList) {
            for (PacketField field : fieldList.getFields()) {
                Object value = data.getValue(field.getName());
                if (value != null)
                    field.write(buf, value);
            }
        }
    }

}
