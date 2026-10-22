package com.aero.client.api;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Saves module on/off + settings to config/aero-client.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean dirty, loading;
    private static long lastSave;

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("aero-client.json"); }
    public static void markDirty() { if (!loading) dirty = true; }

    public static void load() {
        loading = true;
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            JsonObject root = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            for (Module m : Modules.get().all()) {
                JsonObject mo = root.has(m.name) ? root.getAsJsonObject(m.name) : null;
                if (mo == null) continue;
                JsonObject so = mo.has("settings") ? mo.getAsJsonObject("settings") : new JsonObject();
                for (Setting<?> s : m.settings.all()) {
                    if (!so.has(s.name)) continue;
                    s.load(fromJson(so.get(s.name)));
                }
                if (mo.has("enabled") && mo.get("enabled").getAsBoolean()) m.setActive(true);
            }
        } catch (Throwable t) { Log.error("config load failed", t); }
        finally { loading = false; dirty = false; }
    }

    public static void save() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            for (Module m : Modules.get().all()) {
                Map<String, Object> mo = new LinkedHashMap<>();
                mo.put("enabled", m.isActive());
                Map<String, Object> so = new LinkedHashMap<>();
                for (Setting<?> s : m.settings.all()) so.put(s.name, s.save());
                mo.put("settings", so);
                root.put(m.name, mo);
            }
            Files.writeString(file(), GSON.toJson(root));
            dirty = false;
        } catch (Throwable t) { Log.error("config save failed", t); }
    }

    /** Called every tick: debounced autosave. */
    public static void tick() {
        long now = System.currentTimeMillis();
        if (dirty && now - lastSave > 2000) { lastSave = now; save(); }
    }

    private static Object fromJson(JsonElement e) {
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isNumber()) return p.getAsNumber();
            return p.getAsString();
        }
        if (e.isJsonArray()) {
            java.util.List<Object> l = new java.util.ArrayList<>();
            for (JsonElement x : e.getAsJsonArray()) l.add(fromJson(x));
            return l;
        }
        return null;
    }
    private Config() {}
}
