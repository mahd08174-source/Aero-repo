package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.staff.StaffDetector;
import com.aero.client.staff.StaffEntry;
import com.aero.client.staff.StaffTracker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.aero.client.core.events.GameLeftEvent;
import com.aero.client.core.events.Render2DEvent;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.ColorSetting;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.settings.StringSetting;
import com.aero.client.core.Module;
import com.aero.client.core.render.SettingColor;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.lwjgl.glfw.GLFW;

public class StaffList
extends Module {
    private static final int C_BG = -267580147;
    private static final int C_BG_GRAD = 0x6FFFFFF;
    private static final int C_BORDER = 0x1AFFFFFF;
    private static final int C_SHINE = 0x8FFFFFF;
    private static final int C_TEXT = -1;
    private static final int C_MUTED = -12961222;
    private static final int C_OFFLINE = -10395295;
    private static final int ICON_SZ = 14;
    private static final int PAD = 12;
    private static final int ROW_H = 22;
    private static final int W = 224;
    private static final int HDR_H = 26;
    private static final int R = 16;
    private final SettingGroup sg;
    private final Setting<SettingColor> accentColor;
    private final Setting<String> detectBy;
    private final Setting<String> staffNames;
    private final Setting<String> highlightNames;
    private final Setting<String> rankKeywords;
    private final Setting<String> starSymbols;
    private final Setting<Boolean> fontIcons;
    private final Setting<Boolean> showVanished;
    private final Setting<Boolean> showRank;
    private final Setting<Boolean> showPing;
    private final Setting<Boolean> chatAlerts;
    private final Setting<Integer> maxRows;
    public final Setting<Double> scale;
    private int panelX;
    private int panelY;
    private boolean dragging;
    private int dragOffX;
    private int dragOffY;
    private boolean lastMouseDown;
    private final StaffTracker tracker;
    private final LinkedHashMap<String, AnimEntry> animEntries;
    private long enableTime;
    private int lastBadge;
    private long badgeAnimTime;

    public StaffList() {
        super(AeroCategory.HUD_V1, "staff-list", "Shows server staff detected from tab list.");
        this.sg = this.settings.getDefaultGroup();
        this.accentColor = this.sg.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("accent-color")).description("Widget accent color \u2014 affects border glow, badge, stripe.")).defaultValue(new SettingColor(229, 57, 53, 255)).build());
        this.detectBy = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("detect-by")).description("Detection mode: 'Star Only', 'Rank Only', 'Star + Rank', 'Names Only'.")).defaultValue("Star Only")).build());
        this.staffNames = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("staff-names")).description("Comma-separated player names to always flag.")).defaultValue("")).build());
        this.highlightNames = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("highlight-names")).description("Comma-separated names to pin to top and highlight (e.g. dangerous staff).")).defaultValue("W1zoX_")).build());
        this.rankKeywords = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("rank-keywords")).description("Comma-separated rank keywords.")).defaultValue(StaffDetector.DEFAULT_RANK_KEYWORDS_STRING)).build());
        this.starSymbols = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("star-symbols")).description("Characters used as staff markers in tab list.")).defaultValue("\u2605\u2606\u2726\u2727\u272a\u2729\u272b\u272c\u272d\u272e\u272f\u2b50\u2730\u2742\u269d\u2734\u2735\u2736\u2737\u2738\u2739\u235f\u2764\u2665\u25c6\u25cf\u25b6\u26a1\u2714\u2718\u00bb\u00ab\u2023\u203a\u2039\u00b7\u2022\u25aa\u25ab")).build());
        this.fontIcons = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("font-icons")).description("Treat private-use Unicode chars as markers.")).defaultValue(true)).build());
        this.showVanished = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-vanished")).description("Show vanished/spectator staff.")).defaultValue(true)).build());
        this.showRank = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-rank")).description("Show rank badge.")).defaultValue(true)).build());
        this.showPing = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-ping")).description("Show ping and signal bars.")).defaultValue(true)).build());
        this.chatAlerts = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-alerts")).description("Alert in chat when staff joins.")).defaultValue(false)).build());
        this.maxRows = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-rows")).description("Maximum rows to display.")).defaultValue(10)).min(1).max(30).sliderRange(1, 15).build());
        this.scale = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("HUD scale multiplier.")).defaultValue(1.0).min(0.5).max(3.0).sliderRange(0.5, 2.5).build());
        this.panelX = 5;
        this.panelY = 120;
        this.dragging = false;
        this.dragOffX = 0;
        this.dragOffY = 0;
        this.lastMouseDown = false;
        this.tracker = new StaffTracker(this);
        this.animEntries = new LinkedHashMap();
        this.enableTime = 0L;
        this.lastBadge = -1;
        this.badgeAnimTime = 0L;
    }

    public void onActivate() {
        this.tracker.reset();
        this.animEntries.clear();
        this.enableTime = System.currentTimeMillis();
        this.lastBadge = -1;
    }

    public void onDeactivate() {
        this.tracker.clear();
        this.animEntries.clear();
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

    @EventHandler
    private void onTick(TickEvent.Post event) {
        AnimEntry ae;
        if (this.mc.field_1724 == null) {
            return;
        }
        this.tracker.tick();
        List<StaffEntry> current = this.tracker.current();
        HashSet<String> curNames = new HashSet<String>();
        for (StaffEntry staffEntry : current) {
            curNames.add(staffEntry.name());
        }
        for (Map.Entry entry : this.animEntries.entrySet()) {
            ae = (AnimEntry)entry.getValue();
            if (curNames.contains(entry.getKey()) || ae.leaving()) continue;
            ae.leaveTime = System.currentTimeMillis();
        }
        for (StaffEntry staffEntry : current) {
            if (!this.animEntries.containsKey(staffEntry.name())) {
                this.animEntries.put(staffEntry.name(), new AnimEntry(staffEntry));
                continue;
            }
            ae = this.animEntries.get(staffEntry.name());
            ae.onLatencyUpdate(staffEntry.latency());
            ae.entry = staffEntry;
            ae.leaveTime = 0L;
        }
        this.animEntries.entrySet().removeIf(me -> ((AnimEntry)me.getValue()).done());
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.tracker.clear();
        this.animEntries.clear();
    }

    public void onStaffAppear(StaffEntry e) {
        if (((Boolean)this.chatAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
            String rank = e.rankLabel().isEmpty() ? "Staff" : e.rankLabel();
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7c[StaffList] \u00a7f" + e.name() + " \u00a77(" + rank + ") \u00a7fjoined")), false);
        }
    }

    public StaffDetector.DetectConfig detectConfig() {
        HashSet<String> names = new HashSet<String>();
        for (String s : ((String)this.staffNames.get()).split(",")) {
            String t = s.strip().toLowerCase(Locale.ROOT);
            if (t.isEmpty()) continue;
            names.add(t);
        }
        ArrayList<String> kws = new ArrayList<String>();
        for (String s : ((String)this.rankKeywords.get()).split(",")) {
            String t = s.strip().toLowerCase(Locale.ROOT);
            if (t.isEmpty()) continue;
            kws.add(t);
        }
        return new StaffDetector.DetectConfig((String)this.detectBy.get(), names, kws, (String)this.starSymbols.get(), (Boolean)this.fontIcons.get(), (Boolean)this.showVanished.get());
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        float badgeT;
        if (this.mc.field_1724 == null) {
            return;
        }
        ArrayList<AnimEntry> visible = new ArrayList<AnimEntry>();
        for (AnimEntry animEntry : this.animEntries.values()) {
            if (!animEntry.done()) {
                visible.add(animEntry);
            }
            if (visible.size() < (Integer)this.maxRows.get()) continue;
            break;
        }
        HashSet<String> highlights = new HashSet<String>();
        for (String s : ((String)this.highlightNames.get()).split(",")) {
            String t = s.strip();
            if (t.isEmpty()) continue;
            highlights.add(t.toLowerCase(Locale.ROOT));
        }
        visible.sort((a, b) -> {
            boolean bH;
            boolean aH = !a.leaving() && highlights.contains(a.entry.name().toLowerCase(Locale.ROOT));
            boolean bl = bH = !b.leaving() && highlights.contains(b.entry.name().toLowerCase(Locale.ROOT));
            if (aH != bH) {
                return aH ? -1 : 1;
            }
            return 0;
        });
        long l = System.currentTimeMillis();
        float sc = ((Double)this.scale.get()).floatValue();
        int visRows = Math.min(this.animEntries.size(), (Integer)this.maxRows.get());
        int panelH = (int)((float)(26 + visRows * 22 + 12) * sc);
        int panelW = (int)(224.0f * sc);
        int sw2 = this.mc.method_22683().method_4486();
        int sh2 = this.mc.method_22683().method_4502();
        this.panelX = class_3532.method_15340((int)this.panelX, (int)0, (int)Math.max(0, sw2 - panelW));
        this.panelY = class_3532.method_15340((int)this.panelY, (int)0, (int)Math.max(0, sh2 - panelH));
        this.handleDrag(panelW, panelH);
        int px = this.panelX;
        int py = this.panelY;
        int online = 0;
        for (AnimEntry ae : this.animEntries.values()) {
            if (ae.leaving()) continue;
            ++online;
        }
        int accentPacked = ((SettingColor)this.accentColor.get()).getPacked();
        int accentRGB = accentPacked & 0xFFFFFF;
        float widgetT = Math.min(1.0f, (float)(l - this.enableTime) / 400.0f);
        float widgetEO = StaffList.easeOut(widgetT);
        int widgetA = (int)(widgetEO * 255.0f);
        float widgetSl = (1.0f - widgetEO) * -16.0f;
        if (online != this.lastBadge) {
            this.badgeAnimTime = l;
            this.lastBadge = online;
        }
        float badgeScale = (badgeT = Math.min(1.0f, (float)(l - this.badgeAnimTime) / 220.0f)) < 0.5f ? 1.0f + StaffList.easeOut(badgeT * 2.0f) * 0.15f : 1.15f - StaffList.easeOut((badgeT - 0.5f) * 2.0f) * 0.15f;
        int contentH = 0;
        for (AnimEntry animEntry : visible) {
            contentH += animEntry.rowHeight();
        }
        int cardH = 32 + contentH + 12;
        if (cardH < 38) {
            cardH = 38;
        }
        class_332 class_3322 = event.drawContext;
        class_3322.method_51448().pushMatrix();
        class_3322.method_51448().translate((float)px + widgetSl, (float)py);
        class_3322.method_51448().scale(sc, sc);
        StaffList.fillR(class_3322, 5, 6, 224, cardH, 16, StaffList.alphaMul(0x55000000, widgetA));
        StaffList.fillR(class_3322, 3, 4, 224, cardH, 16, StaffList.alphaMul(0x30000000, widgetA));
        StaffList.fillR(class_3322, 1, 2, 224, cardH, 16, StaffList.alphaMul(0x15000000, widgetA));
        if (online > 0) {
            StaffList.fillR(class_3322, -4, -4, 232, cardH + 8, 20, StaffList.alphaMul(0x4000000 | accentRGB, widgetA));
            StaffList.fillR(class_3322, -3, -3, 230, cardH + 6, 19, StaffList.alphaMul(0xA000000 | accentRGB, widgetA));
            StaffList.fillR(class_3322, -2, -2, 228, cardH + 4, 18, StaffList.alphaMul(0x15000000 | accentRGB, widgetA));
            StaffList.fillR(class_3322, -1, -1, 226, cardH + 2, 17, StaffList.alphaMul(0x20000000 | accentRGB, widgetA));
        }
        StaffList.fillR(class_3322, 0, 0, 224, cardH, 16, StaffList.alphaMul(-267580147, widgetA));
        int gradH = Math.min(cardH / 2, 30);
        StaffList.fillR(class_3322, 1, 1, 222, gradH, 15, StaffList.alphaMul(0x6FFFFFF, widgetA));
        StaffList.fillR(class_3322, 0, 0, 224, cardH, 16, StaffList.alphaMul(0x1AFFFFFF, widgetA));
        StaffList.fillR(class_3322, 1, 1, 222, cardH - 2, 15, StaffList.alphaMul(-267580147, widgetA));
        StaffList.fillR(class_3322, 2, 2, 220, 4, 14, StaffList.alphaMul(0x8FFFFFF, widgetA));
        int stripeColor = StaffList.alphaMul(0xFF000000 | accentRGB, widgetA);
        StaffList.fillR(class_3322, 0, 16, 3, cardH - 32, 0, stripeColor);
        class_3322.method_25294(2, 15, 3, 16, stripeColor);
        class_3322.method_25294(2, cardH - 16, 3, cardH - 16 + 1, stripeColor);
        int hdrY = 7;
        class_3322.method_51433(this.mc.field_1772, "STAFF ONLINE", 12, hdrY, StaffList.alphaMul(-1, widgetA), false);
        Object badgeStr = online < 10 ? "0" + online : String.valueOf(online);
        int bw = this.mc.field_1772.method_1727((String)badgeStr) + 10;
        int bx = 212 - bw;
        int by = hdrY - 1;
        int bh = 13;
        class_3322.method_51448().pushMatrix();
        float bcx = (float)bx + (float)bw / 2.0f;
        float bcy = (float)by + (float)bh / 2.0f;
        class_3322.method_51448().translate(bcx, bcy);
        class_3322.method_51448().scale(badgeScale, badgeScale);
        class_3322.method_51448().translate(-bcx, -bcy);
        int numCol = online > 0 ? StaffList.alphaMul(0xFF000000 | accentRGB, widgetA) : StaffList.alphaMul(-12961222, widgetA);
        class_3322.method_51433(this.mc.field_1772, (String)badgeStr, bx + 5, by + 2, numCol, false);
        class_3322.method_51448().popMatrix();
        class_3322.method_25294(12, 26, 212, 27, StaffList.alphaMul(0x18FFFFFF, widgetA));
        if (visible.isEmpty()) {
            class_3322.method_51433(this.mc.field_1772, "No staff detected", 26, 34, StaffList.alphaMul(-12961222, widgetA), false);
            class_3322.method_51448().popMatrix();
            return;
        }
        int rowY = 32;
        for (AnimEntry animEntry : visible) {
            int symColor;
            String symbol;
            int rh = animEntry.rowHeight();
            if (rh <= 0) continue;
            int rowAlpha = Math.min(widgetA, animEntry.alpha());
            float slideY = animEntry.slideY();
            boolean pinned = !animEntry.leaving() && highlights.contains(animEntry.entry.name().toLowerCase(Locale.ROOT));
            class_3322.method_51448().pushMatrix();
            class_3322.method_51448().translate(0.0f, slideY);
            if (!animEntry.leaving() && rh == 22) {
                if (pinned) {
                    StaffList.fillR(class_3322, 12, rowY, 200, rh - 2, 4, StaffList.alphaMul(0x28000000 | accentRGB, rowAlpha));
                    class_3322.method_25294(12, rowY, 14, rowY + rh - 2, StaffList.alphaMul(0xFF000000 | accentRGB, rowAlpha));
                } else {
                    StaffList.fillR(class_3322, 12, rowY, 200, rh - 2, 4, StaffList.alphaMul(0xCFFFFFF, rowAlpha));
                }
            }
            int midY = rowY + rh / 2;
            int iconX = 16;
            int iconY = rowY + (rh - 7) / 2;
            String rank = animEntry.entry.rankLabel();
            int marker = animEntry.entry.color();
            if (!rank.isEmpty()) {
                symbol = rank.substring(0, Math.min(1, rank.length())).toUpperCase();
                symColor = StaffList.rankColor(rank) | 0xFF000000;
            } else if ((marker & 0xFFFFFF) != 0) {
                symbol = "\u2605";
                symColor = 0xFF000000 | marker & 0xFFFFFF;
            } else {
                symbol = "\u2605";
                symColor = 0xFF000000 | accentRGB;
            }
            class_3322.method_51433(this.mc.field_1772, symbol, iconX, iconY, StaffList.alphaMul(symColor, rowAlpha), false);
            int dotX = iconX + this.mc.field_1772.method_1727("\u2605") + 5;
            int dotY = midY - 2;
            boolean vanished = animEntry.entry.vanished();
            if (!vanished) {
                float pulse = animEntry.dotPulse();
                int glowA1 = (int)(pulse * 60.0f);
                int glowA2 = (int)(pulse * 100.0f);
                class_3322.method_25294(dotX - 2, dotY - 2, dotX + 6, dotY + 6, StaffList.alphaMul(4431943, Math.min(rowAlpha, glowA1)));
                class_3322.method_25294(dotX - 1, dotY - 1, dotX + 5, dotY + 5, StaffList.alphaMul(4431943, Math.min(rowAlpha, glowA2)));
                class_3322.method_25294(dotX, dotY, dotX + 4, dotY + 4, StaffList.alphaMul(-12345273, rowAlpha));
            } else {
                class_3322.method_25294(dotX, dotY, dotX + 4, dotY + 4, StaffList.alphaMul(-10395295, rowAlpha));
            }
            int rightX = 212;
            if (((Boolean)this.showPing.get()).booleanValue() && rh == 22) {
                int barsX = rightX - 14;
                StaffList.drawSignalBars(class_3322, barsX, rowY + rh / 2 - 4, animEntry.entry.latency(), rowAlpha);
                rightX = barsX - 4;
            }
            int nameX = dotX + 8;
            int nameAvail = rightX - nameX;
            if (((Boolean)this.showRank.get()).booleanValue() && !animEntry.entry.rankLabel().isEmpty()) {
                String rank2 = animEntry.entry.rankLabel();
                int rw2 = this.mc.field_1772.method_1727(rank2) + 6;
                nameAvail -= rw2 + 3;
                int rx2 = rightX - rw2;
                if (!((Boolean)this.showPing.get()).booleanValue() && rh == 22) {
                    StaffList.fillR(class_3322, rx2, rowY + 4, rw2, 11, 3, StaffList.alphaMul(StaffList.rankColor(rank2), rowAlpha));
                    class_3322.method_51433(this.mc.field_1772, rank2, rx2 + 3, rowY + 5, StaffList.alphaMul(-1, rowAlpha), false);
                }
            }
            int nameColor = vanished ? -10395295 : (pinned ? 0xFF000000 | accentRGB : -1);
            class_3322.method_51433(this.mc.field_1772, this.clip(animEntry.entry.name(), nameAvail), nameX, midY - 4, StaffList.alphaMul(nameColor, rowAlpha), pinned);
            class_3322.method_51448().popMatrix();
            rowY += rh;
        }
        class_3322.method_51448().popMatrix();
    }

    private static void drawSignalBars(class_332 ctx, int x, int y, int ms, int alpha) {
        int quality = ms < 60 ? 4 : (ms < 120 ? 3 : (ms < 200 ? 2 : 1));
        int colGood = -12345273;
        int colOk = -19712;
        int colBad = -1754827;
        int barCol = ms < 60 ? colGood : (ms < 120 ? colOk : colBad);
        for (int i = 0; i < 4; ++i) {
            int bh = 3 + i * 2;
            int bx = x + i * 4;
            int by = y + (9 - bh);
            int col = i < quality ? StaffList.alphaMul(barCol, alpha) : StaffList.alphaMul(-12961222, alpha);
            ctx.method_25294(bx, by, bx + 2, by + bh, col);
        }
    }

    private static int rankColor(String rank) {
        return switch (rank.toLowerCase(Locale.ROOT)) {
            case "owner" -> -1061741540;
            case "admin" -> -1058719435;
            case "manager" -> -1057987950;
            case "mod", "moderator", "srmod" -> -1072073006;
            case "helper" -> -1069309881;
            case "dev", "developer" -> -1065672798;
            case "partner" -> -1057652969;
            default -> -1069196700;
        };
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

    private static int pingColor(int ms) {
        if (ms < 50) {
            return -12345273;
        }
        if (ms < 100) {
            return -19712;
        }
        return -1754827;
    }

    private static int alphaMul(int color, int a255) {
        if (a255 <= 0) {
            return color & 0xFFFFFF;
        }
        if (a255 >= 255) {
            return color;
        }
        int origA = color >>> 24 & 0xFF;
        int newA = origA * a255 / 255;
        return color & 0xFFFFFF | newA << 24;
    }

    private static int lerpRGB(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab2 = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb2 = b & 0xFF;
        int r = ar + (int)((float)(br - ar) * t);
        int g = ag + (int)((float)(bg - ag) * t);
        int bl = ab2 + (int)((float)(bb2 - ab2) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    private static float easeOut(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
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
        for (int i = 0; i < r; ++i) {
            double d = Math.sqrt(Math.max(0.0, (double)(r * r) - (double)((r - 1 - i) * (r - 1 - i))));
            int in = r - (int)d;
            ctx.method_25294(x + in, y + i, x + r, y + i + 1, color);
            ctx.method_25294(x + w - r, y + i, x + w - in, y + i + 1, color);
            ctx.method_25294(x + in, y + h - i - 1, x + r, y + h - i, color);
            ctx.method_25294(x + w - r, y + h - i - 1, x + w - in, y + h - i, color);
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

    private static final class AnimEntry {
        StaffEntry entry;
        final long joinTime;
        long leaveTime;
        int prevLatency;
        long latencyChangeTime;

        AnimEntry(StaffEntry e) {
            this.entry = e;
            this.joinTime = System.currentTimeMillis();
            this.prevLatency = e.latency();
        }

        boolean leaving() {
            return this.leaveTime > 0L;
        }

        boolean done() {
            return this.leaving() && AnimEntry.age(this.leaveTime) > 380L;
        }

        int rowHeight() {
            if (!this.leaving()) {
                return 22;
            }
            float t = AnimEntry.clamp01((float)AnimEntry.age(this.leaveTime) / 280.0f);
            return Math.max(0, (int)(22.0f * (1.0f - AnimEntry.easeIn(t))));
        }

        int alpha() {
            if (this.leaving()) {
                return (int)((1.0f - AnimEntry.clamp01((float)AnimEntry.age(this.leaveTime) / 300.0f)) * 255.0f);
            }
            return (int)(AnimEntry.easeOut(AnimEntry.clamp01((float)AnimEntry.age(this.joinTime) / 250.0f)) * 255.0f);
        }

        float slideY() {
            if (this.leaving()) {
                return -AnimEntry.easeIn(AnimEntry.clamp01((float)AnimEntry.age(this.leaveTime) / 280.0f)) * 10.0f;
            }
            return (1.0f - AnimEntry.easeOut(AnimEntry.clamp01((float)AnimEntry.age(this.joinTime) / 300.0f))) * 6.0f;
        }

        float dotPulse() {
            if (this.leaving()) {
                return 0.5f;
            }
            double t = (double)System.currentTimeMillis() / 1400.0 * Math.PI * 2.0;
            return (float)(0.5 + 0.5 * Math.sin(t));
        }

        int latencyColor() {
            int cur = StaffList.pingColor(this.entry.latency());
            if (this.latencyChangeTime == 0L) {
                return cur;
            }
            float t = AnimEntry.clamp01((float)AnimEntry.age(this.latencyChangeTime) / 450.0f);
            if (t >= 1.0f) {
                return cur;
            }
            return StaffList.lerpRGB(StaffList.pingColor(this.prevLatency), cur, AnimEntry.easeOut(t));
        }

        void onLatencyUpdate(int newMs) {
            if (newMs != this.entry.latency()) {
                this.prevLatency = this.entry.latency();
                this.latencyChangeTime = System.currentTimeMillis();
            }
        }

        static float easeOut(float t) {
            return 1.0f - (1.0f - t) * (1.0f - t);
        }

        static float easeIn(float t) {
            return t * t;
        }

        static float clamp01(float t) {
            return Math.max(0.0f, Math.min(1.0f, t));
        }

        static long age(long since) {
            return System.currentTimeMillis() - since;
        }
    }
}

