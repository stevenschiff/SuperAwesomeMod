package com.steveplays.superawesomemod;

public final class AppleSkinData {
    private static boolean enabled = false;

    private AppleSkinData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
