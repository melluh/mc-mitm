package com.melluh.mcmitm.protocol;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProtocolCodec {

    private final int protocolId;
    private final Map<ProtocolState, ProtocolStateCodec> stateCodecs = new EnumMap<>(ProtocolState.class);

    public ProtocolCodec(int protocolId) {
        this.protocolId = protocolId;
    }

    public void registerStateCodec(ProtocolState state, ProtocolStateCodec codec) {
        stateCodecs.put(state, codec);
    }

    public ProtocolStateCodec getStateCodec(ProtocolState state) {
        return stateCodecs.get(state);
    }

    public int getProtocolId() {
        return protocolId;
    }

    public static class ProtocolStateCodec {

        private final Map<Integer, PacketType> clientboundPackets = new HashMap<>();
        private final Map<Integer, PacketType> serverboundPackets = new HashMap<>();

        public void registerPacket(PacketType packet) {
            switch(packet.getDirection()) {
                case CLIENTBOUND -> clientboundPackets.put(packet.getId(), packet);
                case SERVERBOUND -> serverboundPackets.put(packet.getId(), packet);
            }
        }

        public PacketType getPacket(PacketDirection direction, int id) {
            return switch(direction) {
                case CLIENTBOUND -> clientboundPackets.get(id);
                case SERVERBOUND -> serverboundPackets.get(id);
            };
        }

        public PacketType getClientboundPacket(int id) {
            return clientboundPackets.get(id);
        }

        public PacketType getServerboundPacket(int id) {
            return serverboundPackets.get(id);
        }

        public List<PacketType> getClientboundPackets() {
            return clientboundPackets.entrySet().stream()
                    .sorted(Entry.comparingByKey())
                    .map(Entry::getValue)
                    .toList();
        }

        public List<PacketType> getServerboundPackets() {
            return serverboundPackets.entrySet().stream()
                    .sorted(Entry.comparingByKey())
                    .map(Entry::getValue)
                    .toList();
        }

    }

   public static ProtocolCodec loadFromJson(JsonObject json) {
        int protocolId = json.getObject("version").getInt("protocol");
        ProtocolCodec codec = new ProtocolCodec(protocolId);

        JsonObject packetsJson = json.getObject("packets");
        for(ProtocolState state : ProtocolState.values()) {
            ProtocolStateCodec stateCodec = new ProtocolStateCodec();
            codec.registerStateCodec(state, stateCodec);

            JsonArray statePacketsJson = packetsJson.getArray(state.name().toLowerCase());
            for(Object obj : statePacketsJson) {
                JsonObject packetJson = (JsonObject) obj;
                try {
                    PacketType packetType = PacketType.create(packetJson);
                    stateCodec.registerPacket(packetType);
                } catch (Throwable throwable) {
                    Logger.error(throwable, "Failed to initialize packet type {}", packetJson.getString("name"));
                }
            }
        }

        return codec;
   }

   public enum PacketDirection {

        CLIENTBOUND("C->S"), SERVERBOUND("S->C");

        private final String name;

        PacketDirection(String name) {
            this.name = name;
        }

       public String getName() {
           return name;
       }

       public static PacketDirection parse(String str) {
            if(str.equals("server"))
                return SERVERBOUND;
            if(str.equals("client"))
                return CLIENTBOUND;

            throw new IllegalArgumentException("Invalid packet direction: " + str);
        }

   }

}
