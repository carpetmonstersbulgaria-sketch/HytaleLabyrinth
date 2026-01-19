package com.labyrinth.core.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public abstract class ConfigBase {

    private Map<String, Object> config;
    private File configFile;
    private Gson gson;

    protected ConfigBase(String fileName) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.configFile = new File("plugins/Labyrinth", fileName + ".json");
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!configFile.exists()) {
                copyDefaultConfig();
            }

            String json = Files.readString(configFile.toPath());
            config = gson.fromJson(json, Map.class);

        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config", e);
        }
    }

    private void copyDefaultConfig() throws IOException {
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        String resourceName = "/" + configFile.getName();
        InputStream defaultConfig = getClass().getResourceAsStream(resourceName);

        if (defaultConfig != null) {
            Files.copy(defaultConfig, configFile.toPath());
            defaultConfig.close();
        } else {
            config = new java.util.HashMap<>();
            save();
        }
    }

    public void save() {
        try {
            String json = gson.toJson(config);
            Files.writeString(configFile.toPath(), json);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config", e);
        }
    }

    public String getString(String path, String defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof String) {
            return (String) value;
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    public int getInt(String path, int defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    public double getDouble(String path, double defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path, List<String> defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof List) {
            return (List<String>) value;
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getObject(String path, Map<String, Object> defaultValue) {
        Object value = getNestedValue(path);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        set(path, defaultValue);
        save();
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (!(next instanceof Map)) {
                next = new java.util.HashMap<>();
                current.put(keys[i], next);
            }
            current = (Map<String, Object>) next;
        }

        current.put(keys[keys.length - 1], value);
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return null;
            }
        }

        return current.get(keys[keys.length - 1]);
    }
}
