package com.steveplays.superawesomemod;

public class ArmorHudData {
    private static boolean enabled = false;
    /** Scale factor 1-5 (1 = default 16px icons, 5 = 80px icons). */
    private static int scale = 1;

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getScale()      { return scale; }
    public static void setScale(int s) { scale = Math.clamp(s, 1, 5); }
}
