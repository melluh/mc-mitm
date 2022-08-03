package com.melluh.mcmitm.protocol.field;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.packet.PacketData;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.function.Function;

public abstract class PacketField {

    private final FieldType type;
    private final String name;
    private final String notes;
    private final PacketFieldCondition condition;

    public PacketField(FieldType type, JsonObject json) {
        this.type = type;
        this.name = json.getString("name");
        this.notes = json.getString("notes");
        this.condition = json.has("if") ? PacketFieldCondition.create(json.getObject("if")) : null;
    }

    public abstract Object read(ByteBuf buf, PacketData parentData) throws IOException;
    public abstract void write(ByteBuf buf, Object data) throws IOException;

    public FieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public PacketFieldCondition getCondition() {
        return condition;
    }

    public static PacketField create(JsonObject json) {
        FieldType fieldType = FieldType.valueOf(json.getString("type").toUpperCase());
        return fieldType.create(json);
    }

    public enum FieldType {

        INT(IntField::new),
        LONG(LongField::new),
        VARINT(VarIntField::new),
        VARLONG(VarLongField::new),
        STRING(StringField::new),
        SHORT(ShortField::new),
        BYTE(ByteField::new),
        UBYTE(UnsignedByteField::new),
        FLOAT(FloatField::new),
        DOUBLE(DoubleField::new),
        BOOLEAN(BooleanField::new),
        BYTE_ARRAY(ByteArrayField::new),
        UNSIGNED_BYTE_ARRAY(UnsignedByteArray::new),
        UUID(UuidField::new),
        BLOCKPOS(BlockPosField::new),
        COLLECTION(CollectionField::new),
        BYTES(BytesField::new),
        NBT(NbtField::new),
        ITEM(ItemField::new);

        private final Function<JsonObject, PacketField> createFunction;

        FieldType(Function<JsonObject, PacketField> createFunction) {
            this.createFunction = createFunction;
        }

        public PacketField create(JsonObject json) {
            return createFunction.apply(json);
        }

    }

}
