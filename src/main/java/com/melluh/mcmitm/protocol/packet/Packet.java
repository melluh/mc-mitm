package com.melluh.mcmitm.protocol.packet;

import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.field.PacketFieldCondition;
import io.netty.buffer.ByteBuf;

public class Packet {

    private final PacketType type;
    private final PacketData data = new PacketData(this, null);

    public Packet(PacketType type) {
        this.type = type;
    }

    public void write(ByteBuf buf) {
        for (PacketField field : type.getFields()) {
            field.write(buf, data.getValue(field.getName()));
        }
    }

    public void read(ByteBuf buf) {
        for(PacketField field : type.getFields()) {
            PacketFieldCondition condition = field.getCondition();
            if(condition != null && !condition.evaluate(data))
                continue;

            data.addValue(field.getName(), field.read(buf));
        }
    }

    public PacketType getType() {
        return type;
    }

    public PacketData getData() {
        return data;
    }

}
