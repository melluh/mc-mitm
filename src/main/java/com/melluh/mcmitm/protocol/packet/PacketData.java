package com.melluh.mcmitm.protocol.packet;

import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.field.PacketFieldCondition;
import com.melluh.mcmitm.protocol.field.PacketFieldList;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketData {

    private final PacketFieldList fieldList;
    private final PacketData parent;
    private final Map<String, Object> values = new HashMap<>();

    public PacketData(PacketFieldList fieldList, PacketData parent) {
        this.fieldList = fieldList;
        this.parent = parent;
    }

    public void read(ByteBuf buf) throws IOException  {
        for(PacketField field : fieldList.getFields()) {
            PacketFieldCondition condition = field.getCondition();
            if(condition != null && !condition.evaluate(this))
                continue;

            this.addValue(field.getName(), field.read(buf, this));
        }
    }

    private void addValue(String name, Object value) {
        values.put(name, value);
    }

    public void write(ByteBuf buf) throws IOException {
        for (PacketField field : fieldList.getFields()) {
            Object value = this.getValue(field.getName());
            if (value != null)
                field.write(buf, value);
        }
    }

    public boolean hasParent() {
        return parent != null;
    }

    public PacketData getParent() {
        return parent;
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
