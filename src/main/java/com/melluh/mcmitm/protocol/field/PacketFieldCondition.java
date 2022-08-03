package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class PacketFieldCondition {

    private final String fieldName;
    private final Mode mode;
    private final List<Object> values;

    public PacketFieldCondition(String fieldName, Mode mode, List<Object> values) {
        this.fieldName = fieldName;
        this.mode = mode;
        this.values = values;
    }

    public boolean evaluate(PacketData data) {
        return this.evaluate(data, fieldName);
    }

    public boolean evaluate(PacketData data, String fieldName) {
        if(fieldName.startsWith("../")) {
            if(!data.hasParent())
                throw new IllegalStateException("Data does not have parent");
            return this.evaluate(data.getParent(), fieldName.substring(3));
        }

        Object value = data.getValue(fieldName);
        if(value == null)
            return false;

        if(mode == Mode.EQUALS) {
            return values.stream().map(String::valueOf)
                    .anyMatch(str -> str.equals(String.valueOf(value)));
        }

        if(mode == Mode.NOT_EQUAL) {
            return values.stream().map(String::valueOf)
                    .noneMatch(str -> str.equals(String.valueOf(value)));
        }

        if(mode == Mode.BITMASK) {
            int intVal = (int) value;
            return values.stream()
                    .map(val -> Integer.decode((String) val))
                    .anyMatch(mask -> (intVal & mask) > 0);
        }

        return false;
    }

    public enum Mode {
        EQUALS, NOT_EQUAL, BITMASK;
    }

    public static PacketFieldCondition create(JsonObject json) {
        for(Mode mode : Mode.values()) {
            String modeName = mode.name().toLowerCase();
            if(json.has(modeName))
                return new PacketFieldCondition(json.getString("field"), mode, getValues(json, modeName));
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
