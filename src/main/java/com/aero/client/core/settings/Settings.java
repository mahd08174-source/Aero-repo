package com.aero.client.core.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** All setting groups of one module. */
public final class Settings {
    private final List<SettingGroup> groups = new ArrayList<>();
    private final SettingGroup defaultGroup = createGroup("General");

    public SettingGroup getDefaultGroup() { return defaultGroup; }

    public SettingGroup createGroup(String name) {
        for (SettingGroup g : groups) if (g.name.equals(name)) return g;
        SettingGroup g = new SettingGroup(name);
        groups.add(g);
        return g;
    }

    public List<SettingGroup> getGroups() { return Collections.unmodifiableList(groups); }
}
