package com.aero.client.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module {
    public final Category category;
    public final String name, title, description;
    public final Settings settings = new Settings();
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean active;

    public Module(Category category, String name, String description) {
        this.category = category; this.name = name; this.description = description;
        StringBuilder sb = new StringBuilder();
        for (String p : name.split("-")) if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        this.title = sb.toString().trim();
    }

    public boolean isActive() { return active; }
    public void toggle() { setActive(!active); }
    public void enable() { setActive(true); }
    public void disable() { setActive(false); }

    public void setActive(boolean on) {
        if (on == active) return;
        active = on;
        if (on) { EventBus.register(this); onActivate(); }
        else { EventBus.unregister(this); onDeactivate(); }
        Config.markDirty();
    }

    public void onActivate() {}
    public void onDeactivate() {}
    public String getInfoString() { return null; }

    public void info(String msg, Object... args) { chat("\u00a7b", msg, args); }
    public void warning(String msg, Object... args) { chat("\u00a7e", msg, args); }
    public void error(String msg, Object... args) { chat("\u00a7c", msg, args); }

    private void chat(String color, String msg, Object[] args) {
        if (mc.inGameHud == null) return;
        String m = (args != null && args.length > 0) ? String.format(msg, args) : msg;
        mc.inGameHud.getChatHud().addMessage(Text.literal("\u00a77[\u00a7bAero\u00a77] " + color + title + "\u00a7r " + m));
    }
}
