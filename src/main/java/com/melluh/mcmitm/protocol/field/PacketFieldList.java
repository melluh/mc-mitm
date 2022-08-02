package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketFieldList {

    private final PacketFieldList parent;
    private final List<PacketField> fields;

    public PacketFieldList(PacketFieldList parent, List<PacketField> fields) {
        this.parent = parent;
        this.fields = fields;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public PacketFieldList getParent() {
        return parent;
    }

    public List<PacketField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public static PacketFieldList create(JsonArray json, PacketFieldList parent) {
        List<PacketField> fields = new ArrayList<>();
        for(Object obj : json) {
            JsonObject fieldJson = (JsonObject) obj;
            fields.add(PacketField.create(fieldJson));
        }
        return new PacketFieldList(parent, fields);
    }

}
