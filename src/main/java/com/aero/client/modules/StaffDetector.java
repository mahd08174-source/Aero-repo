package com.aero.client.modules;

import com.aero.client.AeroCategory;
import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.Set;
import com.aero.client.core.events.TickEvent;
import com.aero.client.core.settings.BoolSetting;
import com.aero.client.core.settings.Setting;
import com.aero.client.core.settings.SettingGroup;
import com.aero.client.core.settings.StringSetting;
import com.aero.client.core.Module;
import com.aero.client.core.events.EventHandler;
import net.minecraft.class_2561;
import net.minecraft.class_640;

public class StaffDetector
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<String> staffPrefixes;
    private final Setting<Boolean> alertOnJoin;
    private final Setting<Boolean> alertOnLeave;
    private final Set<String> knownStaff;

    public StaffDetector() {
        super(AeroCategory.MISC_V2, "staff-detector", "Monitors the player list for names matching staff prefixes and alerts you when staff join or leave the server.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.staffPrefixes = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("staff-prefixes")).description("Comma-separated keywords to match against player names and display names. Case-insensitive.")).defaultValue("Admin,Mod,Staff,Owner,Dev,Helper,Support,Builder,Manager")).build());
        this.alertOnJoin = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("alert-on-join")).description("Send a chat alert when a matched player appears in the player list.")).defaultValue(true)).build());
        this.alertOnLeave = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("alert-on-leave")).description("Send a chat alert when a matched player disappears from the player list.")).defaultValue(true)).build());
        this.knownStaff = new HashSet<String>();
    }

    public void onActivate() {
        this.knownStaff.clear();
        if (this.mc.method_1562() == null) {
            return;
        }
        for (class_640 entry : this.mc.method_1562().method_2880()) {
            String name = this.getProfileName(entry);
            if (!this.isStaff(name)) continue;
            this.knownStaff.add(name);
        }
    }

    public void onDeactivate() {
        this.knownStaff.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.method_1562() == null) {
            return;
        }
        HashSet<String> current = new HashSet<String>();
        for (class_640 entry : this.mc.method_1562().method_2880()) {
            String display;
            String name = this.getProfileName(entry);
            String string = display = entry.method_2971() != null ? entry.method_2971().getString() : name;
            if (!this.isStaff(name) && !this.isStaff(display)) continue;
            current.add(name);
        }
        if (((Boolean)this.alertOnJoin.get()).booleanValue()) {
            for (String name : current) {
                if (this.knownStaff.contains(name)) continue;
                this.alert("\u00a7c\u00a7l\u26a0 STAFF JOINED: \u00a7r\u00a7e" + name);
            }
        }
        if (((Boolean)this.alertOnLeave.get()).booleanValue()) {
            for (String name : this.knownStaff) {
                if (current.contains(name)) continue;
                this.alert("\u00a7a\u00a7l\u2714 Staff left: \u00a7r\u00a77" + name);
            }
        }
        this.knownStaff.clear();
        this.knownStaff.addAll(current);
    }

    public String getInfoString() {
        return this.knownStaff.isEmpty() ? null : this.knownStaff.size() + " staff";
    }

    private boolean isStaff(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lower = name.toLowerCase();
        for (String prefix : ((String)this.staffPrefixes.get()).split(",")) {
            String p = prefix.trim().toLowerCase();
            if (p.isEmpty() || !lower.contains(p)) continue;
            return true;
        }
        return false;
    }

    private void alert(String msg) {
        if (this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a78[\u00a7bStaffDetector\u00a78] \u00a7r" + msg)), false);
        }
    }

    private String getProfileName(class_640 entry) {
        GameProfile profile = entry.method_2966();
        try {
            return (String)profile.getClass().getMethod("getName", new Class[0]).invoke((Object)profile, new Object[0]);
        }
        catch (Exception exception) {
            try {
                return (String)profile.getClass().getMethod("name", new Class[0]).invoke((Object)profile, new Object[0]);
            }
            catch (Exception exception2) {
                return "";
            }
        }
    }
}

