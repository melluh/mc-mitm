package com.melluh.mcmitm.util;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    private Utils() {}

    public static JsonObject loadJsonFromFile(String fileName) {
        return loadJsonFromFile(new File(fileName));
    }

    public static JsonObject loadJsonFromFile(File file) {
        try {
            return JsonParser.object().from(new FileReader(file));
        } catch (IOException | JsonParserException ex) {
            Logger.error(ex, "Failed to read {}", file.getName());
            return null;
        }
    }

}
