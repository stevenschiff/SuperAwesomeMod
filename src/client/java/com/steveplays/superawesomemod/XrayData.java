package com.steveplays.superawesomemod;

import java.util.HashSet;
import java.util.Set;

public final class XrayData {

    private static boolean enabled = false;
    private static final Set<String> enabledOres = new HashSet<>();

    private XrayData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isOreEnabled(String name) {
        return enabledOres.contains(name);
    }

    public static void setOreEnabled(String name, boolean on) {
        if (on) enabledOres.add(name);
        else    enabledOres.remove(name);
    }

    public static void toggleOre(String name) {
        if (enabledOres.contains(name)) enabledOres.remove(name);
        else enabledOres.add(name);
    }
}
