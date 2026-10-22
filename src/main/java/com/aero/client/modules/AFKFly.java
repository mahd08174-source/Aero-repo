package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.DoubleSetting;
import com.aero.client.core.settings.IntSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.Module;
import com.aero.client.core.utils.FindItemResult;
import com.aero.client.core.utils.InvUtils;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2848;

public class AFKFly
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> autoStart;
    private final Setting<Double> minSpeed;
    private final Setting<Double> minY;
    private final Setting<Integer> fireworkDelay;
    public final Setting<Boolean> kickOnSusChunk;
    private int fireworkCooldown;

    public AFKFly() {
        super(AeroCategory.MISC_V2, "afk-fly", "Maintains elytra flight automatically by firing rockets from your hotbar when you slow down or drop too low. Safe to leave running AFK.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.autoStart = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-start")).description("Automatically activate elytra flight when you are airborne and have an elytra equipped.")).defaultValue(true)).build());
        this.minSpeed = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-speed")).description("Fire a firework when your horizontal speed (blocks/tick) drops below this value.")).defaultValue(0.6).sliderMin(0.1).sliderMax(2.0).build());
        this.minY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-y")).description("Fire a firework when your Y level drops below this height.")).defaultValue(100.0).sliderMin(-64.0).sliderMax(320.0).build());
        this.fireworkDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("firework-delay")).description("Cooldown in ticks between consecutive firework uses. Prevents wasting rockets.")).defaultValue(20)).sliderMin(5).sliderMax(60).build());
        this.kickOnSusChunk = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("kick-on-sus-chunk")).description("Automatically disconnect and disable AFK Fly when SusChunkFinder detects a suspicious chunk.")).defaultValue(true)).build());
        this.fireworkCooldown = 0;
    }

    public void onActivate() {
        this.fireworkCooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        boolean tooLow;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.fireworkCooldown > 0) {
            --this.fireworkCooldown;
        }
        if (((Boolean)this.autoStart.get()).booleanValue() && !this.mc.field_1724.method_6128()) {
            if (!this.mc.field_1724.method_24828() && this.hasElytraEquipped()) {
                this.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)this.mc.field_1724, class_2848.class_2849.field_12982));
            }
            return;
        }
        if (!this.mc.field_1724.method_6128()) {
            return;
        }
        double hSpeed = Math.sqrt(this.mc.field_1724.method_18798().field_1352 * this.mc.field_1724.method_18798().field_1352 + this.mc.field_1724.method_18798().field_1350 * this.mc.field_1724.method_18798().field_1350);
        boolean tooSlow = hSpeed < (Double)this.minSpeed.get();
        boolean bl = tooLow = this.mc.field_1724.method_23318() < (Double)this.minY.get();
        if ((tooSlow || tooLow) && this.fireworkCooldown <= 0) {
            this.useFirework();
        }
    }

    private void useFirework() {
        if (this.mc.field_1724 == null || this.mc.field_1761 == null) {
            return;
        }
        FindItemResult firework = InvUtils.findInHotbar((class_1792[])new class_1792[]{class_1802.field_8639});
        if (!firework.found()) {
            return;
        }
        InvUtils.swap((int)firework.slot(), (boolean)false);
        this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
        InvUtils.swapBack();
        this.fireworkCooldown = (Integer)this.fireworkDelay.get();
    }

    private boolean hasElytraEquipped() {
        if (this.mc.field_1724 == null) {
            return false;
        }
        return this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
    }

    public String getInfoString() {
        if (this.mc.field_1724 == null) {
            return null;
        }
        return this.mc.field_1724.method_6128() ? "Flying" : "Not flying";
    }
}

