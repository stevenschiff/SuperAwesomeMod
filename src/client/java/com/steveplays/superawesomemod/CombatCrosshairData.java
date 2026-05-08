package com.steveplays.superawesomemod;

public final class CombatCrosshairData {
    private static boolean enabled = false;

    private CombatCrosshairData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
