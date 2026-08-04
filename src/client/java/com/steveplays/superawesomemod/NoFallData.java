package com.steveplays.superawesomemod;

public final class NoFallData {
    private static boolean enabled = false;

    private NoFallData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
