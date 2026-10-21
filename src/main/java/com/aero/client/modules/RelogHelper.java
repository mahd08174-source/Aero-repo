package com.aero.client.modules;

import com.aero.client.AeroCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2561;

public class RelogHelper
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> triggerY;
    private final Setting<String> message;
    private final Setting<Integer> delayTicks;
    private final Setting<Boolean> clearChat;
    private int tickCounter;
    private boolean wasBelow;

    public RelogHelper() {
        super(AeroCategory.MISC_V2, "relog-helper", "Spams a red message only to you while below the set Y coordinate.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.triggerY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("y-coordinate")).description("Spams the message while below this Y level.")).defaultValue(-1.0).sliderMin(-64.0).sliderMax(320.0).build());
        this.message = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("message")).description("Message shown only to you in red.")).defaultValue("relog and go up")).build());
        this.delayTicks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay-ticks")).description("Ticks between each message (20 = 1 second).")).defaultValue(20)).sliderMin(1).sliderMax(100).build());
        this.clearChat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("clear-chat")).description("Clears chat when you go above the Y coordinate.")).defaultValue(true)).build());
        this.tickCounter = 0;
        this.wasBelow = false;
    }

    public void onActivate() {
        this.tickCounter = 0;
        this.wasBelow = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        boolean below;
        if (this.mc.field_1724 == null) {
            return;
        }
        boolean bl = below = this.mc.field_1724.method_23318() <= (Double)this.triggerY.get();
        if (!below) {
            if (this.wasBelow && ((Boolean)this.clearChat.get()).booleanValue()) {
                this.mc.field_1705.method_1743().method_1808(false);
            }
            this.wasBelow = false;
            this.tickCounter = 0;
            return;
        }
        this.wasBelow = true;
        if (++this.tickCounter < (Integer)this.delayTicks.get()) {
            return;
        }
        this.tickCounter = 0;
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a78[\u00a7c\u00a7lRelog\u00a78] \u00a7r\u00a7f" + (String)this.message.get())), false);
    }
}

