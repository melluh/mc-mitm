package com.melluh.mcmitm.protocol;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class ProtocolCodec {

    private final int protocolId;
    private final List<String> releases;
    private final Map<ProtocolState, ProtocolStateCodec> stateCodecs = new EnumMap<>(ProtocolState.class);

    public ProtocolCodec(int protocolId, List<String> releases) {
        this.protocolId = protocolId;
        this.releases = releases;
    }

    public String getDisplayName() {
        return protocolId + " (" + String.join("/", releases) + ")";
    }

    public void registerStateCodec(ProtocolState state, ProtocolStateCodec codec) {
        stateCodecs.put(state, codec);
    }

    public List<PacketType> getAllPacketTypes() {
        return this.getStateCodecs().stream()
                .flatMap(stateCodec -> stateCodec.getPacketTypes().stream())
                .toList();
    }

    public List<ProtocolStateCodec> getStateCodecs() {
        return stateCodecs.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .map(Entry::getValue)
                .toList();
    }

    public ProtocolStateCodec getStateCodec(ProtocolState state) {
        return stateCodecs.get(state);
    }

    public int getProtocolId() {
        return protocolId;
    }

    public static class ProtocolStateCodec {

        private final ProtocolState state;
        private final List<PacketType> clientboundPackets = new ArrayList<>();
        private final List<PacketType> serverboundPackets = new ArrayList<>();

        public ProtocolStateCodec(ProtocolState state) {
            this.state = state;
        }

        public ProtocolState getState() {
            return state;
        }

        public void registerPacketType(PacketType packet) {
            List<PacketType> list = this.getPacketTypes(packet.getDirection());
            list.add(packet);
            packet.setId(list.indexOf(packet));
        }

        public PacketType getPacketType(PacketDirection direction, int id) {
            return this.getPacketTypes(direction).get(id);
        }

        public PacketType getPacketType(PacketDirection direction, String name) {
            return this.getPacketTypes(direction).stream()
                    .filter(type -> type.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        private List<PacketType> getPacketTypes(PacketDirection direction) {
            return switch(direction) {
                case CLIENTBOUND -> clientboundPackets;
                case SERVERBOUND -> serverboundPackets;
            };
        }

        public List<PacketType> getPacketTypes() {
            return Stream.concat(clientboundPackets.stream(), serverboundPackets.stream())
                    .toList();
        }

    }

   public static ProtocolCodec loadFromJson(JsonObject json) {
        JsonObject versionJson = json.getObject("version");
        int protocolId = versionJson.getInt("protocol");
        List<String> releases = versionJson.getArray("releases").stream().map(String::valueOf).toList();
        ProtocolCodec codec = new ProtocolCodec(protocolId, releases);

        JsonObject packetsJson = json.getObject("packets");
        for(ProtocolState state : ProtocolState.values()) {
            ProtocolStateCodec stateCodec = new ProtocolStateCodec(state);
            codec.registerStateCodec(state, stateCodec);

            JsonArray statePacketsJson = packetsJson.getArray(state.name().toLowerCase());
            for(Object obj : statePacketsJson) {
                JsonObject packetJson = (JsonObject) obj;
                try {
                    PacketType packetType = PacketType.create(packetJson, state);
                    stateCodec.registerPacketType(packetType);
                } catch (Exception ex) {
                    Logger.error(ex, "Failed to initialize packet type {}", packetJson.getString("name"));
                }
            }
        }

        return codec;
   }

   public enum PacketDirection {

        CLIENTBOUND("S->C"), SERVERBOUND("C->S");

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
