package com.melluh.mcmitm.protocol.packet;

import com.melluh.mcmitm.protocol.PacketType;

public class Packet {

    private final PacketType type;
    private final PacketData data;

    public Packet(PacketType type) {
        this.type = type;
        this.data = new PacketData(type.getFieldList(), null);
    }

    public PacketType getType() {
        return type;
    }

    public PacketData getData() {
        return data;
    }

}
