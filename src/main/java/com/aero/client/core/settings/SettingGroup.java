package com.aero.client.core.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A titled list of settings shown together in the settings screen. */
public final class SettingGroup {
    public final String name;
    private final List<Setting<?>> settings = new ArrayList<>();

    SettingGroup(String name) {
        this.name = name;
    }

    public <T> Setting<T> add(Setting<T> setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getAll() { return Collections.unmodifiableList(settings); }
}
