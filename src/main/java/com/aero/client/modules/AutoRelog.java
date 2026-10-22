package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_2561;

public class AutoRelog
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> voidLevel;
    private final Setting<Integer> gracePeriod;
    private final Setting<Boolean> logToChat;
    private int graceTicks;
    private boolean inGrace;
    private boolean hasWarned;

    public AutoRelog() {
        super(AeroCategory.MISC_V2, "auto-relog", "Kicks you at void Y. Has grace period on join so it doesn't kick you immediately.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.voidLevel = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("void-level")).description("Y level at which to disconnect.")).defaultValue(-60.0).sliderMin(-128.0).sliderMax(0.0).build());
        this.gracePeriod = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("grace-period")).description("Seconds after joining where void kick is disabled.")).defaultValue(16)).sliderMin(1).sliderMax(60).build());
        this.logToChat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("log-to-chat")).defaultValue(true)).build());
        this.graceTicks = 0;
        this.inGrace = false;
        this.hasWarned = false;
    }

    public void onActivate() {
        this.graceTicks = (Integer)this.gracePeriod.get() * 20;
        this.inGrace = true;
        this.hasWarned = false;
        this.chat("\u00a7aGrace period started (" + String.valueOf(this.gracePeriod.get()) + "s)");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.inGrace) {
            if (--this.graceTicks <= 0) {
                this.inGrace = false;
                this.chat("\u00a7eGrace period ended, void protection active.");
            }
            return;
        }
        double y = this.mc.field_1724.method_23318();
        if (y < (Double)this.voidLevel.get() + 5.0 && !this.hasWarned) {
            this.hasWarned = true;
            this.chat("\u00a7eApproaching void! Y: " + String.format("%.1f", y));
        }
        if (y < (Double)this.voidLevel.get()) {
            this.chat("\u00a7cVoid at Y" + String.format("%.1f", y) + " \u2014 kicking...");
            this.hasWarned = false;
            if (this.mc.method_1562() != null && this.mc.method_1562().method_48296() != null) {
                this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)"[AutoRelog] Void"));
            }
        }
    }

    private void chat(String msg) {
        if (((Boolean)this.logToChat.get()).booleanValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7c[AutoRelog] \u00a7f" + msg)), false);
        }
    }
}

