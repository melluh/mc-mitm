package com.melluh.mcmitm.util;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.grack.nanojson.JsonWriterException;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Utils {

    private Utils() {}

    public static void saveDefaultFile(File file) {
        try(InputStream in = Utils.class.getClassLoader().getResourceAsStream(file.getName())) {
            if(in == null) {
                Logger.error("Failed to find {} in JAR", file.getName());
                return;
            }

            Files.copy(in, file.toPath());
        } catch (IOException ex) {
            Logger.error(ex, "Failed to save default {}", file.getName());
        }
    }

    public static JsonArray jsonArrayFromFile(String fileName) {
        return jsonArrayFromFile(new File(fileName));
    }

    public static JsonArray jsonArrayFromFile(File file) {
        try {
            return JsonParser.array().from(new FileReader(file));
        } catch (IOException | JsonParserException ex) {
            Logger.error(ex, "Failed to read {}", file.getName());
            return new JsonArray();
        }
    }

    public static JsonObject jsonObjectFromFile(String fileName) {
        return jsonObjectFromFile(new File(fileName));
    }

    public static JsonObject jsonObjectFromFile(File file) {
        try {
            return JsonParser.object().from(new FileReader(file));
        } catch (IOException | JsonParserException ex) {
            Logger.error(ex, "Failed to read {}", file.getName());
            return new JsonObject();
        }
    }

    public static void writeJson(File file, Object object) {
        try {
            Files.writeString(file.toPath(), JsonWriter.string(object));
        } catch (IOException | JsonWriterException ex) {
            Logger.error(ex, "Failed to write {}", file.getName());
        }
    }

    public static String formatLength(int bytes) {
        if (bytes > -1000 && bytes < 1000)
            return bytes + " B";

        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000;
            ci.next();
        }

        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
