package com.steveplays.superawesomemod;

public final class InvincibilityData {

    private static boolean enabled = false;

    private InvincibilityData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
