package com.steveplays.superawesomemod;

public final class HigherCrouchData {
    private static boolean enabled = false;

    private HigherCrouchData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
