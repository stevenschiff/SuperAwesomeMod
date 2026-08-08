package com.steveplays.superawesomemod;

public final class NoSlowData {

    private static boolean enabled = false;

    private NoSlowData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
