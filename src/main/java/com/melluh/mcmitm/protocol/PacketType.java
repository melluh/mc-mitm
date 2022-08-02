package com.melluh.mcmitm.protocol;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.field.PacketFieldList;

import java.util.List;

public class PacketType {

    private final int id;
    private final String name;
    private final PacketDirection direction;
    private final PacketFieldList fieldList;

    public PacketType(int id, String name, PacketDirection direction, PacketFieldList fieldList) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.fieldList = fieldList;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PacketDirection getDirection() {
        return direction;
    }

    public List<PacketField> getFields() {
        return fieldList.getFields();
    }

    public static PacketType create(JsonObject json) {
        int id = Integer.decode(json.getString("id"));
        String name = json.getString("name");
        PacketDirection direction = PacketDirection.parse(json.getString("to"));
        PacketFieldList fieldList = PacketFieldList.create(json.getArray("fields"), null);
        return new PacketType(id, name, direction, fieldList);
    }

}
