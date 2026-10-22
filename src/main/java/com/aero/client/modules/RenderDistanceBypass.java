package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.TickEvent;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.EventHandler;

public class RenderDistanceBypass
extends Module {
    private final SettingGroup sg;
    private final Setting<Integer> triggerY;
    private final Setting<Integer> tempDistance;
    private final Setting<Integer> restoreDistance;
    private final Setting<Integer> restoreDelay;
    private boolean triggered;
    private boolean waiting;
    private int ticksLeft;
    private int ticksAbove;

    public RenderDistanceBypass() {
        super(AeroCategory.MISC_V2, "render-distance-bypass", "Reduces render distance when falling below trigger Y, then restores it.");
        this.sg = this.settings.getDefaultGroup();
        this.triggerY = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("trigger-y")).description("Activates when player Y is at or below this value.")).defaultValue(-1)).range(-64, -1).sliderRange(-64, -1).build());
        this.tempDistance = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("temp-distance")).description("Render distance set when triggered.")).defaultValue(2)).range(2, 32).sliderRange(2, 32).build());
        this.restoreDistance = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("restore-distance")).description("Render distance restored after the delay.")).defaultValue(32)).range(2, 64).sliderRange(2, 64).build());
        this.restoreDelay = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("restore-delay")).description("Ticks to wait before restoring render distance.")).defaultValue(20)).range(1, 100).sliderRange(1, 100).build());
        this.triggered = false;
        this.waiting = false;
        this.ticksLeft = 0;
        this.ticksAbove = 0;
    }

    public void onActivate() {
        this.triggered = false;
        this.waiting = false;
        this.ticksLeft = 0;
        this.ticksAbove = 0;
    }

    public void onDeactivate() {
        this.triggered = false;
        this.waiting = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        boolean belowTrigger;
        if (this.mc.player == null || this.mc.options == null) {
            return;
        }
        double y = this.mc.player.getY();
        boolean bl = belowTrigger = y < (double)((Integer)this.triggerY.get()).intValue();
        if (belowTrigger) {
            this.ticksAbove = 0;
        } else {
            ++this.ticksAbove;
            if (this.ticksAbove >= 10) {
                this.triggered = false;
            }
        }
        if (this.waiting) {
            ++this.ticksLeft;
            if (this.ticksLeft >= (Integer)this.restoreDelay.get()) {
                this.mc.options.getViewDistance().setValue(((Integer)this.restoreDistance.get()));
                this.mc.options.sendClientSettings();
                this.waiting = false;
            }
            return;
        }
        if (belowTrigger && !this.triggered) {
            this.triggered = true;
            this.mc.options.getViewDistance().setValue(((Integer)this.tempDistance.get()));
            this.mc.options.sendClientSettings();
            this.ticksLeft = 0;
            this.waiting = true;
        }
    }
}

