package com.aero.client;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.HashMap;
import java.util.Map;

/** Text in the bundled Xuong font (aero:xuong / aero:xuong_big); falls back to the vanilla font if parsing fails. */
public final class AeroText {
    private static final Map<String, Text> CACHE = new HashMap<>();

    private static Text styled(String s, String font) {
        if (CACHE.size() > 600) CACHE.clear();
        return CACHE.computeIfAbsent(font + "|" + s, k -> {
            try {
                JsonObject o = new JsonObject();
                o.addProperty("text", s);
                o.addProperty("font", "aero:" + font);
                var r = TextCodecs.CODEC.parse(JsonOps.INSTANCE, o).result();
                if (r.isPresent()) return r.get();
            } catch (Throwable ignored) { }
            return Text.literal(s);
        });
    }

    public static int width(String s) { return width(s, "xuong"); }
    public static int width(String s, String font) { return MinecraftClient.getInstance().textRenderer.getWidth(styled(s, font)); }

    public static void draw(DrawContext ctx, String s, int x, int y, int color) { draw(ctx, s, "xuong", x, y, color); }
    public static void draw(DrawContext ctx, String s, String font, int x, int y, int color) {
        ctx.drawText(MinecraftClient.getInstance().textRenderer, styled(s, font), x, y, color, false);
    }
    private AeroText() {}
}
