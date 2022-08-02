package com.melluh.mcmitm.protocol.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketData {

    private final Packet packet;
    private final PacketData parent;
    private final Map<String, Object> values = new HashMap<>();

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

    public void addValue(String name, Object value) {
        values.put(name, value);
    }

    public Object getValue(String name) {
        return values.get(name);
    }

}
