package com.steveplays.superawesomemod;

public final class CombatPotionEffectsData {
    private static boolean enabled = false;

    private CombatPotionEffectsData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
