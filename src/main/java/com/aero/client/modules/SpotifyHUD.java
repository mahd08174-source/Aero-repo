package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.spotify.SpotifyService;
import com.aero.client.spotify.SpotifyState;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.lwjgl.glfw.GLFW;

public class SpotifyHUD
extends Module {
    private static final class_2960 ART_ID = class_2960.method_60655((String)"p3nis", (String)"spotify_art");
    private static final int C_BG = -267580147;
    private static final int C_BG2 = 0x8FFFFFF;
    private static final int C_BORDER = 0x20FFFFFF;
    private static final int C_SHINE = 0x9FFFFFF;
    private static final int C_ACCENT = -14829228;
    private static final int C_TEXT = -1;
    private static final int C_SUB = -6645094;
    private static final int C_MUTED = -12566464;
    private static final int C_PROG_BG = -14803426;
    private static final int C_ART_BG = -15658735;
    private static final int R = 16;
    private static final int ART = 76;
    private static final int PAD = 14;
    private static final int W = 310;
    private static final int H = 104;
    private final SettingGroup sg;
    public final Setting<Double> scale;
    private int panelX;
    private int panelY;
    private boolean dragging;
    private int dragOffX;
    private int dragOffY;
    private boolean lastMouseDown;
    public final Setting<Boolean> hideWhenIdle;
    private SpotifyService service;
    private class_1043 artTex;
    private int loadedArtVer;
    private String lastTitle;
    private long songChangeTime;
    private float scrollX;
    private long scrollStart;
    private String scrollTitle;

    public SpotifyHUD() {
        super(AeroCategory.HUD_V1, "spotify-hud", "Displays the currently playing song via Windows SMTC.");
        this.sg = this.settings.getDefaultGroup();
        this.scale = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("HUD scale multiplier.")).defaultValue(1.0).min(0.5).max(3.0).sliderRange(0.5, 2.5).build());
        this.panelX = 5;
        this.panelY = 5;
        this.dragging = false;
        this.dragOffX = 0;
        this.dragOffY = 0;
        this.lastMouseDown = false;
        this.hideWhenIdle = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hide-when-idle")).description("Hide the HUD when nothing is playing.")).defaultValue(true)).build());
        this.artTex = null;
        this.loadedArtVer = -1;
        this.lastTitle = "";
        this.songChangeTime = 0L;
        this.scrollX = 0.0f;
        this.scrollStart = 0L;
        this.scrollTitle = "";
    }

    public void onActivate() {
        this.service = new SpotifyService();
        this.service.start();
    }

    public void onDeactivate() {
        if (this.service != null) {
            this.service.stop();
            this.service = null;
        }
        this.destroyArt();
    }

    public class_2487 toTag() {
        class_2487 tag = super.toTag();
        if (tag == null) {
            tag = new class_2487();
        }
        tag.method_10569("panelX", this.panelX);
        tag.method_10569("panelY", this.panelY);
        return tag;
    }

    public Module fromTag(class_2487 tag) {
        super.fromTag(tag);
        this.panelX = tag.method_10550("panelX").orElse(this.panelX);
        this.panelY = tag.method_10550("panelY").orElse(this.panelY);
        return this;
    }

    private void destroyArt() {
        if (this.artTex != null) {
            try {
                this.mc.method_1531().method_4615(ART_ID);
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.artTex = null;
            this.loadedArtVer = -1;
        }
    }

    private void tryLoadArt(int ver) {
        if (this.service == null) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(this.service.artPath());
            if (bytes.length < 200) {
                return;
            }
            class_1011 img = class_1011.method_4309((InputStream)new ByteArrayInputStream(bytes));
            this.destroyArt();
            this.artTex = new class_1043(() -> "p3nis/spotify_art", img);
            this.artTex.method_4524();
            this.mc.method_1531().method_4616(ART_ID, (class_1044)this.artTex);
            this.loadedArtVer = ver;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        String curTitle;
        if (this.mc.field_1724 == null || this.service == null) {
            return;
        }
        SpotifyState st = this.service.state();
        if (((Boolean)this.hideWhenIdle.get()).booleanValue() && !st.active()) {
            return;
        }
        if (st.active() && st.artVersion() != this.loadedArtVer) {
            this.mc.execute(() -> this.tryLoadArt(st.artVersion()));
        }
        String string = curTitle = st.active() ? st.title() : "";
        if (!curTitle.equals(this.lastTitle)) {
            this.lastTitle = curTitle;
            this.songChangeTime = System.currentTimeMillis();
            this.scrollX = 0.0f;
            this.scrollStart = 0L;
        }
        float sc = ((Double)this.scale.get()).floatValue();
        long now = System.currentTimeMillis();
        int sw = this.mc.method_22683().method_4486();
        int sh = this.mc.method_22683().method_4502();
        int pw = (int)(310.0f * sc);
        int ph = (int)(104.0f * sc);
        this.panelX = class_3532.method_15340((int)this.panelX, (int)0, (int)Math.max(0, sw - pw));
        this.panelY = class_3532.method_15340((int)this.panelY, (int)0, (int)Math.max(0, sh - ph));
        this.handleDrag(pw, ph);
        int px = this.panelX;
        int py = this.panelY;
        float animT = this.songChangeTime > 0L ? Math.min(1.0f, (float)(now - this.songChangeTime) / 400.0f) : 1.0f;
        float slideOff = (1.0f - SpotifyHUD.easeOut(animT)) * -22.0f;
        int fadeA = (int)(SpotifyHUD.easeOut(animT) * 255.0f);
        class_332 ctx = event.drawContext;
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate((float)px + slideOff, (float)py);
        ctx.method_51448().scale(sc, sc);
        SpotifyHUD.fillR(ctx, 4, 5, 310, 104, 16, SpotifyHUD.alphaMul(0x55000000, fadeA));
        SpotifyHUD.fillR(ctx, 3, 4, 310, 104, 16, SpotifyHUD.alphaMul(0x35000000, fadeA));
        SpotifyHUD.fillR(ctx, 2, 3, 310, 104, 16, SpotifyHUD.alphaMul(0x20000000, fadeA));
        SpotifyHUD.fillR(ctx, 1, 2, 310, 104, 16, SpotifyHUD.alphaMul(0x10000000, fadeA));
        SpotifyHUD.fillR(ctx, -4, -4, 318, 112, 20, SpotifyHUD.alphaMul(69056852, fadeA));
        SpotifyHUD.fillR(ctx, -3, -3, 316, 110, 19, SpotifyHUD.alphaMul(169720148, fadeA));
        SpotifyHUD.fillR(ctx, -2, -2, 314, 108, 18, SpotifyHUD.alphaMul(303937876, fadeA));
        SpotifyHUD.fillR(ctx, -1, -1, 312, 106, 17, SpotifyHUD.alphaMul(471710036, fadeA));
        SpotifyHUD.fillR(ctx, 0, 0, 310, 104, 16, SpotifyHUD.alphaMul(-267580147, fadeA));
        SpotifyHUD.fillR(ctx, 1, 1, 308, 52, 15, SpotifyHUD.alphaMul(0x8FFFFFF, fadeA));
        SpotifyHUD.fillR(ctx, 0, 0, 310, 104, 16, SpotifyHUD.alphaMul(0x20FFFFFF, fadeA));
        SpotifyHUD.fillR(ctx, 1, 1, 308, 102, 15, SpotifyHUD.alphaMul(-267580147, fadeA));
        SpotifyHUD.fillR(ctx, 2, 2, 306, 5, 14, SpotifyHUD.alphaMul(0x9FFFFFF, fadeA));
        SpotifyHUD.fillR(ctx, 0, 16, 3, 72, 0, SpotifyHUD.alphaMul(-14829228, fadeA));
        ctx.method_25294(2, 15, 3, 16, SpotifyHUD.alphaMul(-14829228, fadeA));
        ctx.method_25294(2, 88, 3, 89, SpotifyHUD.alphaMul(-14829228, fadeA));
        int artX = 17;
        int artY = 14;
        SpotifyHUD.fillR(ctx, artX + 2, artY + 3, 76, 76, 6, SpotifyHUD.alphaMul(0x70000000, fadeA));
        SpotifyHUD.fillR(ctx, artX + 1, artY + 2, 76, 76, 6, SpotifyHUD.alphaMul(0x40000000, fadeA));
        SpotifyHUD.fillR(ctx, artX - 2, artY - 2, 80, 80, 8, SpotifyHUD.alphaMul(236829012, fadeA));
        SpotifyHUD.fillR(ctx, artX - 1, artY - 1, 78, 78, 7, SpotifyHUD.alphaMul(371046740, fadeA));
        SpotifyHUD.fillR(ctx, artX, artY, 76, 76, 6, SpotifyHUD.alphaMul(-15658735, fadeA));
        if (this.artTex != null && this.loadedArtVer >= 0) {
            ctx.method_70845(ART_ID, artX, artY, artX + 76, artY + 76, 0.0f, 0.0f, 1.0f, 1.0f);
            SpotifyHUD.maskCorners(ctx, artX, artY, 76, 76, 6, SpotifyHUD.alphaMul(-267580147, fadeA));
        } else {
            int noteX = artX + 38 - 4;
            int noteY = artY + 38 - 4;
            ctx.method_51433(this.mc.field_1772, "\u266b", noteX, noteY, SpotifyHUD.alphaMul(0x25FFFFFF, fadeA), false);
        }
        int tx = artX + 76 + 14;
        int avail = 310 - tx - 14;
        String header = st.active() && st.playing() ? "\u25b6  NOW PLAYING" : (st.active() ? "\u23f8  PAUSED" : "\u2014  IDLE");
        int hcol = st.active() && st.playing() ? SpotifyHUD.alphaMul(-14829228, fadeA) : SpotifyHUD.alphaMul(-12566464, fadeA);
        ctx.method_51433(this.mc.field_1772, header, tx, artY, hcol, false);
        String title = st.active() ? st.title() : "Nothing playing";
        String artist = st.active() ? st.artist() : "";
        int titleW = this.mc.field_1772.method_1727(title);
        if (titleW > avail && st.active()) {
            long elapsed;
            if (!title.equals(this.scrollTitle)) {
                this.scrollTitle = title;
                this.scrollX = 0.0f;
                this.scrollStart = now + 1200L;
            }
            if ((elapsed = now - this.scrollStart) > 0L) {
                this.scrollX = (float)elapsed * 30.0f / 1000.0f % (float)(titleW + 20);
            }
            int sx1 = (int)(((float)px + slideOff + (float)tx) * sc);
            int sy1 = (int)((float)(py + artY + 12) * sc);
            int sx2 = (int)(((float)px + slideOff + (float)tx + (float)avail) * sc);
            int sy2 = sy1 + (int)(10.0f * sc);
            ctx.method_44379(sx1, sy1, sx2, sy2);
            ctx.method_51433(this.mc.field_1772, title, tx - (int)this.scrollX, artY + 12, SpotifyHUD.alphaMul(-1, fadeA), true);
            if (this.scrollX > 0.0f) {
                ctx.method_51433(this.mc.field_1772, title, tx - (int)this.scrollX + titleW + 20, artY + 12, SpotifyHUD.alphaMul(-1, fadeA), true);
            }
            ctx.method_44380();
        } else {
            ctx.method_51433(this.mc.field_1772, this.clip(title, avail), tx, artY + 12, SpotifyHUD.alphaMul(-1, fadeA), true);
        }
        ctx.method_51433(this.mc.field_1772, this.clip(artist, avail), tx, artY + 24, SpotifyHUD.alphaMul(-6645094, fadeA), false);
        if (!st.active()) {
            ctx.method_51448().popMatrix();
            return;
        }
        long posMs = st.livePosMs();
        float progress = st.durMs() > 0L ? Math.min(1.0f, (float)posMs / (float)st.durMs()) : 0.0f;
        int barY = artY + 38;
        int barH = 4;
        SpotifyHUD.drawPill(ctx, tx, barY, avail, barH, SpotifyHUD.alphaMul(-14803426, fadeA));
        int filled = Math.round((float)avail * progress);
        if (filled > 1) {
            SpotifyHUD.drawPill(ctx, tx, barY, filled, barH, SpotifyHUD.alphaMul(-14829228, fadeA));
            int dx = tx + filled - 3;
            int dy = barY - 3;
            ctx.method_25294(dx, dy + 1, dx + 6, dy + 2, SpotifyHUD.alphaMul(1344125268, fadeA));
            ctx.method_25294(dx + 1, dy, dx + 5, dy + 7, SpotifyHUD.alphaMul(1344125268, fadeA));
            ctx.method_25294(dx + 1, dy + 1, dx + 5, dy + 6, SpotifyHUD.alphaMul(-1877100204, fadeA));
            ctx.method_25294(dx + 1, dy + 2, dx + 5, dy + 5, SpotifyHUD.alphaMul(-1, fadeA));
            ctx.method_25294(dx + 2, dy + 1, dx + 4, dy + 6, SpotifyHUD.alphaMul(-1, fadeA));
        }
        int timeY = barY + barH + 4;
        String posStr = SpotifyHUD.fmtMs(posMs);
        String durStr = st.durMs() > 0L ? SpotifyHUD.fmtMs(st.durMs()) : "--:--";
        ctx.method_51433(this.mc.field_1772, posStr, tx, timeY, SpotifyHUD.alphaMul(-12566464, fadeA), false);
        ctx.method_51433(this.mc.field_1772, durStr, tx + avail - this.mc.field_1772.method_1727(durStr), timeY, SpotifyHUD.alphaMul(-12566464, fadeA), false);
        ctx.method_51448().popMatrix();
    }

    private void handleDrag(int pw, int ph) {
        boolean over;
        long window = this.mc.method_22683().method_4490();
        double mx = this.mc.field_1729.method_1603() * (double)this.mc.method_22683().method_4486() / (double)this.mc.method_22683().method_4480();
        double my = this.mc.field_1729.method_1604() * (double)this.mc.method_22683().method_4502() / (double)this.mc.method_22683().method_4507();
        boolean mouseDown = GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
        boolean bl = over = mx >= (double)this.panelX && mx <= (double)(this.panelX + pw) && my >= (double)this.panelY && my <= (double)(this.panelY + ph);
        if (mouseDown && !this.lastMouseDown && over) {
            this.dragging = true;
            this.dragOffX = (int)mx - this.panelX;
            this.dragOffY = (int)my - this.panelY;
        }
        if (!mouseDown) {
            this.dragging = false;
        }
        if (this.dragging) {
            this.panelX = (int)mx - this.dragOffX;
            this.panelY = (int)my - this.dragOffY;
        }
        this.lastMouseDown = mouseDown;
    }

    private static float easeOut(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static int alphaMul(int color, int a255) {
        int origA = color >>> 24 & 0xFF;
        int newA = origA * a255 / 255;
        return color & 0xFFFFFF | newA << 24;
    }

    private static void drawPill(class_332 ctx, int x, int y, int w, int h, int color) {
        int inset;
        int i;
        if (w <= 0) {
            return;
        }
        int r2 = h / 2;
        if (w <= r2 * 2) {
            ctx.method_25294(x, y, x + w, y + h, color);
            return;
        }
        ctx.method_25294(x + r2, y, x + w - r2, y + h, color);
        for (i = 0; i < r2; ++i) {
            inset = r2 - (int)Math.sqrt(Math.max(0.0, (double)(r2 * r2) - (double)((r2 - 1 - i) * (r2 - 1 - i))));
            ctx.method_25294(x + inset, y + i, x + r2, y + i + 1, color);
            ctx.method_25294(x + inset, y + h - 1 - i, x + r2, y + h - i, color);
        }
        for (i = 0; i < r2; ++i) {
            inset = r2 - (int)Math.sqrt(Math.max(0.0, (double)(r2 * r2) - (double)((r2 - 1 - i) * (r2 - 1 - i))));
            ctx.method_25294(x + w - r2, y + i, x + w - inset, y + i + 1, color);
            ctx.method_25294(x + w - r2, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    private static void fillR(class_332 ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((r = Math.min(r, Math.min(w, h) / 2)) <= 0) {
            ctx.method_25294(x, y, x + w, y + h, color);
            return;
        }
        ctx.method_25294(x + r, y, x + w - r, y + h, color);
        ctx.method_25294(x, y + r, x + r, y + h - r, color);
        ctx.method_25294(x + w - r, y + r, x + w, y + h - r, color);
        SpotifyHUD.cornerStep(ctx, x, y, w, h, r, color);
    }

    private static void cornerStep(class_332 ctx, int x, int y, int w, int h, int r, int c) {
        for (int i = 0; i < r; ++i) {
            double d = Math.sqrt(Math.max(0.0, (double)(r * r) - (double)((r - 1 - i) * (r - 1 - i))));
            int in = r - (int)d;
            ctx.method_25294(x + in, y + i, x + r, y + i + 1, c);
            ctx.method_25294(x + w - r, y + i, x + w - in, y + i + 1, c);
            ctx.method_25294(x + in, y + h - i - 1, x + r, y + h - i, c);
            ctx.method_25294(x + w - r, y + h - i - 1, x + w - in, y + h - i, c);
        }
    }

    private static void maskCorners(class_332 ctx, int x, int y, int w, int h, int r, int bg) {
        for (int i = 0; i < r; ++i) {
            double d = Math.sqrt(Math.max(0.0, (double)(r * r) - (double)((r - 1 - i) * (r - 1 - i))));
            int in = r - (int)d;
            ctx.method_25294(x, y + i, x + in, y + i + 1, bg);
            ctx.method_25294(x + w - in, y + i, x + w, y + i + 1, bg);
            ctx.method_25294(x, y + h - i - 1, x + in, y + h - i, bg);
            ctx.method_25294(x + w - in, y + h - i - 1, x + w, y + h - i, bg);
        }
    }

    private String clip(String text, int maxPx) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (this.mc.field_1772.method_1727(text) <= maxPx) {
            return text;
        }
        int dw = this.mc.field_1772.method_1727("...");
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (this.mc.field_1772.method_1727(String.valueOf(sb) + String.valueOf(c)) > maxPx - dw) break;
            sb.append(c);
        }
        return String.valueOf(sb) + "...";
    }

    private static String fmtMs(long ms) {
        long s = ms / 1000L;
        return s / 60L + ":" + String.format("%02d", s % 60L);
    }
}

