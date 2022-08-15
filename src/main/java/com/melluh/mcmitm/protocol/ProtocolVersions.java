package com.melluh.mcmitm.protocol;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.util.Utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtocolVersions {

    private ProtocolVersions() {}

    private static final Map<Integer, ProtocolCodec> VERSIONS = new HashMap<>();

    public static void loadAll() {
        JsonArray indexJson = Utils.jsonArrayFromFile("protocol/versions/index.json");
        for(Object obj : indexJson) {
            JsonObject versionJson = (JsonObject) obj;
            load("protocol/versions/" + versionJson.getInt("protocol") + ".json");
        }
    }

    public static void load(String fileName) {
        ProtocolCodec codec = ProtocolCodec.loadFromJson(Utils.jsonObjectFromFile(fileName));
        VERSIONS.put(codec.getProtocolId(), codec);
    }

    public static List<ProtocolCodec> getAll() {
        return VERSIONS.values().stream()
                .sorted(Comparator.comparing(ProtocolCodec::getProtocolId).reversed())
                .toList();
    }

    public static ProtocolCodec getById(int protocolId) {
        return VERSIONS.get(protocolId);
    }

}
