package com.steveplays.superawesomemod;

public final class NoFogData {
    private static boolean enabled = false;

    private NoFogData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
