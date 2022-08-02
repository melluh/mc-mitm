package com.melluh.mcmitm.protocol.packet;

import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.field.PacketFieldList;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class Packet {

    private final PacketType type;
    private final PacketData data = new PacketData(this, null);

    public Packet(PacketType type) {
        this.type = type;
    }

    public void write(ByteBuf buf) {
        List<PacketField> fields = type.getFieldList().getFields();
        for(int i = 0; i < fields.size(); i++) {
            PacketField field = fields.get(i);
            field.write(buf, data.getValue(i));
        }
    }

    public void read(ByteBuf buf) {
        PacketFieldList fieldList = type.getFieldList();
        fieldList.getFields().forEach(field -> data.addValue(field.read(buf)));
    }

    public PacketType getType() {
        return type;
    }

    public PacketData getData() {
        return data;
    }

}
