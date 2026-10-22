package com.aero.client.api;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    private final SettingGroup def = new SettingGroup("General");
    private final List<SettingGroup> groups = new ArrayList<>(List.of(def));
    public SettingGroup getDefaultGroup() { return def; }
    public SettingGroup createGroup(String name) { SettingGroup g = new SettingGroup(name); groups.add(g); return g; }
    public List<Setting<?>> all() {
        List<Setting<?>> out = new ArrayList<>();
        for (SettingGroup g : groups) out.addAll(g.settings);
        return out;
    }
}
