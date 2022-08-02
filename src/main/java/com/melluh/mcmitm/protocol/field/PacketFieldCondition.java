package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import java.util.List;

public class PacketFieldCondition {

    private final Mode mode;
    private final List<Object> values;

    public PacketFieldCondition(Mode mode, List<Object> values) {
        this.mode = mode;
        this.values = values;
    }

    public boolean evaluate() {
        return false;
    }

    public enum Mode {
        EQUALS, NOT_EQUAL, BITMASK;
    }

    public static PacketFieldCondition create(JsonObject json) {
        for(Mode mode : Mode.values()) {
            String fieldName = mode.name().toLowerCase();
            if(json.has(fieldName))
                return new PacketFieldCondition(mode, getValues(json, fieldName));
        }

        throw new IllegalStateException("No recognized condition fields found");
    }

    private static List<Object> getValues(JsonObject json, String field) {
        JsonArray array = json.getArray(field);
        if(array != null)
            return array;
        return List.of(json.get(field));
    }

}
