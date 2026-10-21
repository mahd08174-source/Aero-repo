package com.aero.client.modules;

import com.aero.client.AeroCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AutoRTPHome
extends Module {
    private final SettingGroup sg;
    private final Setting<Integer> triggerY;
    private final Setting<DelHomeSlot> delHomeSlot;
    private final Setting<RtpRegion> rtpRegion;
    private final Setting<Integer> commandDelay;
    private final Setting<Integer> rtpDelay;
    private final Setting<Integer> cooldownSec;
    private int step;
    private long fireAt;
    private long cooldownUntil;
    private boolean wasAboveTrigger;

    public AutoRTPHome() {
        super(AeroCategory.MISC_V2, "auto-rtp-home", "Auto /delhome, /sethome, /rtp, /home when falling below trigger Y.");
        this.sg = this.settings.getDefaultGroup();
        this.triggerY = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("trigger-y")).description("Activates when player Y is at or below this value.")).defaultValue(-1)).range(-64, -1).sliderRange(-64, -1).build());
        this.delHomeSlot = this.sg.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("delhome-slot")).description("Slot number for /delhome <slot>.")).defaultValue(DelHomeSlot.Slot1)).build());
        this.rtpRegion = this.sg.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rtp-region")).description("Region for /rtp.")).defaultValue(RtpRegion.east)).build());
        this.commandDelay = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("command-delay")).description("Delay in ms between commands.")).defaultValue(500)).range(100, 3000).sliderRange(100, 3000).build());
        this.rtpDelay = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rtp-delay")).description("Extra delay in ms between /rtp and /home (server needs time to teleport).")).defaultValue(1600)).range(500, 10000).sliderRange(500, 10000).build());
        this.cooldownSec = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("cooldown")).description("Cooldown in seconds after sequence completes.")).defaultValue(10)).range(0, 60).sliderRange(0, 60).build());
        this.step = 0;
        this.fireAt = 0L;
        this.cooldownUntil = 0L;
        this.wasAboveTrigger = true;
    }

    public void onActivate() {
        this.step = 0;
        this.fireAt = 0L;
        this.cooldownUntil = 0L;
        this.wasAboveTrigger = true;
    }

    public void onDeactivate() {
        this.step = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.step == 5) {
            if (now >= this.cooldownUntil) {
                this.step = 0;
                this.info("Ready.", new Object[0]);
            }
            return;
        }
        if (this.step == 0) {
            double y = this.mc.field_1724.method_23318();
            if (y > (double)((Integer)this.triggerY.get()).intValue()) {
                this.wasAboveTrigger = true;
            }
            if (this.wasAboveTrigger && y <= (double)((Integer)this.triggerY.get()).intValue()) {
                this.wasAboveTrigger = false;
                this.step = 1;
                this.fireAt = now;
                this.info("Triggered at Y=" + (int)y, new Object[0]);
            }
            return;
        }
        if (now < this.fireAt) {
            return;
        }
        if (this.mc.field_1724.field_3944 == null) {
            return;
        }
        int slot = ((DelHomeSlot)((Object)this.delHomeSlot.get())).ordinal() + 1;
        String region = ((RtpRegion)((Object)this.rtpRegion.get())).cmd();
        long delay = ((Integer)this.commandDelay.get()).intValue();
        switch (this.step) {
            case 1: {
                this.send("delhome " + slot);
                this.info("/delhome " + slot, new Object[0]);
                this.step = 2;
                this.fireAt = now + delay;
                break;
            }
            case 2: {
                this.send("sethome");
                this.info("/sethome", new Object[0]);
                this.step = 3;
                this.fireAt = now + delay;
                break;
            }
            case 3: {
                this.send("rtp " + region);
                this.info("/rtp " + region, new Object[0]);
                this.step = 4;
                this.fireAt = now + (long)((Integer)this.rtpDelay.get()).intValue();
                break;
            }
            case 4: {
                this.send("home " + slot);
                this.info("/home " + slot + " \u2014 done, disabling.", new Object[0]);
                this.step = 5;
                this.cooldownUntil = now + (long)((Integer)this.cooldownSec.get()).intValue() * 1000L;
            }
        }
    }

    private void send(String command) {
        if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
            this.mc.field_1724.field_3944.method_45730(command);
        }
    }

    private void sendChat(String message) {
        if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
            this.mc.field_1724.field_3944.method_45729(message);
        }
    }

    public static enum DelHomeSlot {
        Slot1,
        Slot2,
        Slot3,
        Slot4,
        Slot5,
        Slot6;

    }

    public static enum RtpRegion {
        east,
        west,
        eu_central,
        eu_west,
        asia,
        oceania;


        public String cmd() {
            return this.name().replace('_', '-');
        }
    }
}

