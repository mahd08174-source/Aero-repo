package com.aero.client.core;

import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2487;
import net.minecraft.class_310;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Saves and restores which modules are on and their settings (config/aero-client.json). */
public final class ModuleConfig {
    private static final Set<String> PENDING_ACTIVE = new HashSet<>();
    private static boolean dirty;
    private static long lastSave;

    private ModuleConfig() {}

    private static final String[] EXTRA_INT_KEYS = {"panelX", "panelY"};

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("aero-client.json"); }

    public static void markDirty() { dirty = true; }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            JsonObject root = JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Map.Entry<String, JsonElement> e : mods.entrySet()) {
                Module m = Modules.get().get(e.getKey());
                if (m == null || !e.getValue().isJsonObject()) continue;
                JsonObject o = e.getValue().getAsJsonObject();
                if (o.has("active") && o.get("active").getAsBoolean()) PENDING_ACTIVE.add(m.name);
                if (o.has("extra") && o.get("extra").isJsonObject()) {
                    JsonObject ex = o.getAsJsonObject("extra");
                    class_2487 tag = new class_2487();
                    for (String k : EXTRA_INT_KEYS) if (ex.has(k)) tag.method_10569(k, ex.get(k).getAsInt());
                    m.fromTag(tag);
                }
                if (!o.has("settings")) continue;
                JsonObject s = o.getAsJsonObject("settings");
                for (SettingGroup g : m.settings.getGroups()) {
                    for (Setting<?> setting : g.getAll()) {
                        String key = g.name + "/" + setting.name;
                        if (s.has(key)) setting.deserialize(s.get(key).getAsString());
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[Aero] Could not load config: " + t);
        }
    }

    public static synchronized void save() {
        try {
            JsonObject mods = new JsonObject();
            for (Module m : Modules.get().getAll()) {
                JsonObject o = new JsonObject();
                o.addProperty("active", m.isActive() || PENDING_ACTIVE.contains(m.name));
                JsonObject s = new JsonObject();
                for (SettingGroup g : m.settings.getGroups()) {
                    for (Setting<?> setting : g.getAll()) s.addProperty(g.name + "/" + setting.name, setting.serialize());
                }
                o.add("settings", s);
                class_2487 tag = m.toTag();
                if (tag != null) {
                    JsonObject ex = new JsonObject();
                    for (String k : EXTRA_INT_KEYS) {
                        final String key = k;
                        tag.method_10550(key).ifPresent(v -> ex.addProperty(key, v));
                    }
                    o.add("extra", ex);
                }
                mods.add(m.name, o);
            }
            JsonObject root = new JsonObject();
            root.add("modules", mods);
            Path f = file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, root.toString(), StandardCharsets.UTF_8);
            dirty = false;
            lastSave = System.currentTimeMillis();
        } catch (Throwable t) {
            System.err.println("[Aero] Could not save config: " + t);
        }
    }

    /** Called every client tick: restores modules that were on at last exit, and saves changes. */
    public static void tick(class_310 mc) {
        if (!PENDING_ACTIVE.isEmpty() && mc.field_1724 != null) {
            for (String name : new HashSet<>(PENDING_ACTIVE)) {
                Module m = Modules.get().get(name);
                if (m != null) m.setActive(true);
            }
            PENDING_ACTIVE.clear();
        }
        if (dirty && System.currentTimeMillis() - lastSave > 2000) save();
    }
}
