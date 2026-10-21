package com.aero.client;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Draws text in the bundled Xuong font (aero:xuong / aero:xuong_big).
 * Font-styled Text is built through the JSON text codec, and the Text-based draw / width methods are
 * located by signature at runtime. If anything is missing it silently falls back to the vanilla font,
 * so a mapping difference can never crash or break the build.
 */
public final class AeroText {
    private static final Map<String, class_2561> CACHE = new HashMap<>();
    private static Codec<?> codec;
    private static Method drawText, widthText;
    private static boolean resolved;

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> tr = class_310.method_1551().field_1772.getClass();
            for (Method m : class_332.class.getMethods())
                if (m.getParameterCount() == 6 && m.getParameterTypes()[0].isAssignableFrom(tr)
                    && m.getParameterTypes()[1] == class_2561.class
                    && m.getParameterTypes()[2] == int.class && m.getParameterTypes()[3] == int.class
                    && m.getParameterTypes()[4] == int.class && m.getParameterTypes()[5] == boolean.class) drawText = m;
            for (Method m : tr.getMethods())
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == class_2561.class && m.getReturnType() == int.class) widthText = m;
            codec = (Codec<?>) Class.forName("net.minecraft.class_8824").getField("field_46597").get(null);
        } catch (Throwable ignored) { }
    }

    private static class_2561 styled(String s, String font) {
        if (CACHE.size() > 600) CACHE.clear();
        return CACHE.computeIfAbsent(font + "|" + s, k -> {
            if (codec != null) {
                try {
                    JsonObject o = new JsonObject();
                    o.addProperty("text", s);
                    o.addProperty("font", "aero:" + font);
                    Optional<?> r = ((DataResult<?>) codec.parse(JsonOps.INSTANCE, o)).result();
                    if (r.isPresent()) return (class_2561) r.get();
                } catch (Throwable ignored) { }
            }
            return class_2561.method_43470(s);
        });
    }

    public static int width(String s) { return width(s, "xuong"); }
    public static int width(String s, String font) {
        resolve();
        class_310 mc = class_310.method_1551();
        if (widthText != null && codec != null) {
            try { return (int) widthText.invoke(mc.field_1772, styled(s, font)); } catch (Throwable ignored) { }
        }
        return mc.field_1772.method_1727(s);
    }

    public static void draw(class_332 ctx, String s, int x, int y, int color) { draw(ctx, s, "xuong", x, y, color); }
    public static void draw(class_332 ctx, String s, String font, int x, int y, int color) {
        resolve();
        class_310 mc = class_310.method_1551();
        if (drawText != null && codec != null) {
            try { drawText.invoke(ctx, mc.field_1772, styled(s, font), x, y, color, false); return; } catch (Throwable ignored) { }
        }
        ctx.method_51433(mc.field_1772, s, x, y, color, false);
    }
    private AeroText() {}
}
