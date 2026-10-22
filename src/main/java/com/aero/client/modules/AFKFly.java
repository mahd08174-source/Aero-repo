package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.TickEvent;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.DoubleSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.Module;
import com.aero.client.api.FindItemResult;
import com.aero.client.api.InvUtils;
import com.aero.client.api.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.fireworkCooldown > 0) {
            --this.fireworkCooldown;
        }
        if (((Boolean)this.autoStart.get()).booleanValue() && !this.mc.player.isGliding()) {
            if (!this.mc.player.isOnGround() && this.hasElytraEquipped()) {
                this.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            return;
        }
        if (!this.mc.player.isGliding()) {
            return;
        }
        double hSpeed = Math.sqrt(this.mc.player.getVelocity().x * this.mc.player.getVelocity().x + this.mc.player.getVelocity().z * this.mc.player.getVelocity().z);
        boolean tooSlow = hSpeed < (Double)this.minSpeed.get();
        boolean bl = tooLow = this.mc.player.getY() < (Double)this.minY.get();
        if ((tooSlow || tooLow) && this.fireworkCooldown <= 0) {
            this.useFirework();
        }
    }

    private void useFirework() {
        if (this.mc.player == null || this.mc.interactionManager == null) {
            return;
        }
        FindItemResult firework = InvUtils.findInHotbar((Item[])new Item[]{Items.FIREWORK_ROCKET});
        if (!firework.found()) {
            return;
        }
        InvUtils.swap((int)firework.slot(), (boolean)false);
        this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
        InvUtils.swapBack();
        this.fireworkCooldown = (Integer)this.fireworkDelay.get();
    }

    private boolean hasElytraEquipped() {
        if (this.mc.player == null) {
            return false;
        }
        return this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
    }

    public String getInfoString() {
        if (this.mc.player == null) {
            return null;
        }
        return this.mc.player.isGliding() ? "Flying" : "Not flying";
    }
}

