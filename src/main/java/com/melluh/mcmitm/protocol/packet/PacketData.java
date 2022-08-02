package com.melluh.mcmitm.protocol.packet;

import java.util.ArrayList;
import java.util.List;

public class PacketData {

    private final Packet packet;
    private final PacketData parent;
    private final List<Object> values = new ArrayList<>();

    public PacketData(Packet packet, PacketData parent) {
        this.packet = packet;
        this.parent = parent;
    }

    public Packet getPacket() {
        return packet;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public PacketData getParent() {
        return parent;
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public List<Object> getValues() {
        return values;
    }

}
