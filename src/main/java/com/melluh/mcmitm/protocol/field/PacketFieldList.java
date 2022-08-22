package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketFieldList {

    private final List<PacketField> fields;

    public PacketFieldList(List<PacketField> fields) {
        this.fields = fields;
    }

    public PacketField getField(String name) {
        return fields.stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<PacketField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public static PacketFieldList create(JsonArray json) {
        List<PacketField> fields = new ArrayList<>();
        for(Object obj : json) {
            JsonObject fieldJson = (JsonObject) obj;
            fields.add(PacketField.create(fieldJson));
        }
        return new PacketFieldList(fields);
    }

}
