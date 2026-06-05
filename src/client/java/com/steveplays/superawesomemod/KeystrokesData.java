package com.steveplays.superawesomemod;

public final class KeystrokesData {
    private static boolean enabled = false;

    private KeystrokesData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
