package com.aero.client.core.render;

import com.aero.client.AeroText;
import net.minecraft.class_332;

/** Draws scaled text on the HUD (uses the Aero font). Scale 1.0 is the normal font size. */
public final class TextRenderer {
    private static final TextRenderer INSTANCE = new TextRenderer();
    private boolean active;

    private TextRenderer() {}

    public static TextRenderer get() { return INSTANCE; }

    public void begin(double scale, boolean scaleOnly, boolean big) {
        class_332 ctx = Render2D.context();
        if (ctx == null) return;
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale((float) scale, (float) scale);
        active = true;
    }

    public double getWidth(String text) { return AeroText.width(text); }

    public double getWidth(String text, boolean shadow) { return AeroText.width(text); }

    public double render(String text, double x, double y, Color color, boolean shadow) {
        class_332 ctx = Render2D.context();
        if (ctx == null) return 0;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        if (shadow) {
            int dark = 0xFF000000 | ((color.getPacked() & 0xFCFCFC) >> 2);
            AeroText.draw(ctx, text, px + 1, py + 1, dark);
        }
        AeroText.draw(ctx, text, px, py, color.getPacked() | 0xFF000000);
        return AeroText.width(text);
    }

    public void end() {
        class_332 ctx = Render2D.context();
        if (ctx == null || !active) return;
        ctx.method_51448().popMatrix();
        active = false;
    }
}
