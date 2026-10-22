package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.aero.client.api.TickEvent;
import com.aero.client.api.BoolSetting;
import com.aero.client.api.DoubleSetting;
import com.aero.client.api.IntSetting;
import com.aero.client.api.Setting;
import com.aero.client.api.SettingGroup;
import com.aero.client.api.StringSetting;
import com.aero.client.api.Module;
import com.aero.client.api.EventHandler;
import net.minecraft.text.Text;
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
        if (this.mc.player == null) {
            return;
        }
        boolean bl = below = this.mc.player.getY() <= (Double)this.triggerY.get();
        if (!below) {
            if (this.wasBelow && ((Boolean)this.clearChat.get()).booleanValue()) {
                this.mc.inGameHud.getChatHud().clear(false);
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
        this.mc.player.sendMessage((Text)Text.literal((String)("\u00a78[\u00a7c\u00a7lRelog\u00a78] \u00a7r\u00a7f" + (String)this.message.get())), false);
    }
}

