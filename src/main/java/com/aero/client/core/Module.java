package com.aero.client.core;

import com.aero.client.core.events.Events;
import com.aero.client.core.settings.Settings;
import net.minecraft.class_2561;
import net.minecraft.class_310;

/** Base class of every Aero module. */
public abstract class Module {
    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final Settings settings = new Settings();
    protected final class_310 mc = class_310.method_1551();
    private boolean active;

    public Module(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.title = titleOf(name);
        this.description = description;
    }

    private static String titleOf(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("-")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public void onActivate() {}

    public void onDeactivate() {}

    /** Short status text, or null. */
    public String getInfoString() { return null; }

    public boolean isActive() { return active; }

    public void toggle() { setActive(!active); }

    public void setActive(boolean on) {
        if (on == active) return;
        active = on;
        if (on) {
            Events.subscribe(this);
            try {
                onActivate();
            } catch (Throwable t) {
                System.err.println("[Aero] " + name + " failed to activate: " + t);
                t.printStackTrace();
            }
        } else {
            Events.unsubscribe(this);
            try {
                onDeactivate();
            } catch (Throwable t) {
                System.err.println("[Aero] " + name + " failed to deactivate: " + t);
                t.printStackTrace();
            }
        }
        com.aero.client.core.ModuleConfig.markDirty();
    }

    public void info(String message, Object... args) { chat("§7", message, args); }

    public void warning(String message, Object... args) { chat("§e", message, args); }

    public void error(String message, Object... args) { chat("§c", message, args); }

    private void chat(String color, String message, Object[] args) {
        if (mc.field_1724 == null) return;
        String text = args == null || args.length == 0 ? message : String.format(message, args);
        mc.field_1724.method_7353(class_2561.method_43470("§8[§b" + title + "§8] " + color + text), false);
    }
}
