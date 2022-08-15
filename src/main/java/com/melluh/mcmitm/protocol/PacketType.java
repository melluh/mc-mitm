package com.melluh.mcmitm.protocol;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.field.PacketFieldList;

import java.util.HexFormat;
import java.util.List;

public class PacketType {

    private int id;
    private final String name;
    private final PacketDirection direction;
    private final PacketFieldList fieldList;

    public PacketType(String name, PacketDirection direction, PacketFieldList fieldList) {
        this.name = name;
        this.direction = direction;
        this.fieldList = fieldList;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getHexId() {
        return String.format("0x%1$02X", id);
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

    public PacketFieldList getFieldList() {
        return fieldList;
    }

    public List<PacketField> getFields() {
        return fieldList.getFields();
    }

    public static PacketType create(JsonObject json) {
        String name = json.getString("name");
        PacketDirection direction = PacketDirection.parse(json.getString("to"));
        PacketFieldList fieldList = PacketFieldList.create(json.getArray("fields"));
        return new PacketType(name, direction, fieldList);
    }

}
