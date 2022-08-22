package com.melluh.mcmitm;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.util.Utils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Settings {

    private Settings() {}

    private List<String> filteredPackets;

    public void load() {
        File file = this.getFile();
        if(!file.exists()) {
            Utils.saveDefaultFile(file);
        }

        JsonObject json = Utils.jsonObjectFromFile(file);
        this.filteredPackets = json.getArray("filteredPackets").stream()
                .map(String::valueOf).collect(Collectors.toList());
    }

    public void save() {
        JsonObject json = JsonObject.builder()
                .value("filteredPackets", filteredPackets)
                .done();

        Utils.writeJson(this.getFile(), json);
    }

    private File getFile() {
        return new File("settings.json");
    }

    public boolean isFiltered(PacketType packetType) {
        return filteredPackets.contains(packetType.getState().name() + "/" + packetType.getName());
    }

    public List<String> getFilteredPackets() {
        return filteredPackets;
    }

    private static final Settings INSTANCE = new Settings();

    public static Settings getInstance() {
        return INSTANCE;
    }

}
