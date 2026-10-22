package com.aero.client.api;

import java.util.ArrayList;
import java.util.List;

public class SettingGroup {
    public final String name;
    public final List<Setting<?>> settings = new ArrayList<>();
    public SettingGroup(String name) { this.name = name; }
    public <T> Setting<T> add(Setting<T> s) { settings.add(s); return s; }
}
