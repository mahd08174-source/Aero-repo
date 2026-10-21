package com.aero.client.modules;

import com.aero.client.AeroCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2561;

public class HomeSeter
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> homeNumber;
    private final Setting<Double> delaySeconds;
    private final Setting<Boolean> logToChat;
    private State state;
    private int delayTicks;

    public HomeSeter() {
        super(AeroCategory.MISC_V2, "home-seter", "Sends /delhome <n> then /sethome <n> to update your home position.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.homeNumber = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("home-number")).description("The home number used in /delhome and /sethome.")).defaultValue(1)).sliderMin(1).sliderMax(5).build());
        this.delaySeconds = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("delay-seconds")).description("Delay in seconds between /delhome and /sethome.")).defaultValue(0.5).sliderMin(0.1).sliderMax(10.0).build());
        this.logToChat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("log-to-chat")).description("Show status messages in chat.")).defaultValue(true)).build());
        this.state = State.IDLE;
        this.delayTicks = 0;
    }

    public void onActivate() {
        this.state = State.WAIT_DELHOME;
        this.delayTicks = 0;
    }

    public void onDeactivate() {
        this.state = State.IDLE;
        this.delayTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.method_1562() == null) {
            return;
        }
        switch (this.state.ordinal()) {
            case 1: {
                if (++this.delayTicks < 10) {
                    return;
                }
                this.mc.method_1562().method_45730("delhome " + String.valueOf(this.homeNumber.get()));
                this.state = State.SEND_SETHOME;
                this.delayTicks = 0;
                break;
            }
            case 2: {
                int delayTicksNeeded = (int)((Double)this.delaySeconds.get() * 20.0);
                if (++this.delayTicks < delayTicksNeeded) {
                    return;
                }
                this.mc.method_1562().method_45730("sethome " + String.valueOf(this.homeNumber.get()));
                this.state = State.DONE;
                this.delayTicks = 0;
                break;
            }
            case 3: {
                if (((Boolean)this.logToChat.get()).booleanValue()) {
                    this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7b[HomeSeter] \u00a7aDone!"), false);
                }
                this.toggle();
            }
        }
    }

    private static enum State {
        IDLE,
        WAIT_DELHOME,
        SEND_SETHOME,
        DONE;

    }
}

