package com.steveplays.superawesomemod;

public final class CombatCrosshairData {
    private static boolean enabled = false;

    /** Color index for when aiming at a target in range. */
    private static int inRangeColor = 0;   // default: Red
    /** Color index for when NOT aiming at a target (normal crosshair tint). */
    private static int outOfRangeColor = 7; // default: White

    private CombatCrosshairData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getInRangeColor()      { return inRangeColor; }
    public static void setInRangeColor(int c)  { inRangeColor = Math.clamp(c, 0, CombatHitboxData.COLOR_NAMES.length - 1); }

    public static int  getOutOfRangeColor()      { return outOfRangeColor; }
    public static void setOutOfRangeColor(int c)  { outOfRangeColor = Math.clamp(c, 0, CombatHitboxData.COLOR_NAMES.length - 1); }

    public static String getInRangeColorName()    { return CombatHitboxData.COLOR_NAMES[inRangeColor]; }
    public static String getOutOfRangeColorName() { return CombatHitboxData.COLOR_NAMES[outOfRangeColor]; }

    public static int getInRangeARGB()    { return CombatHitboxData.COLOR_ARGB[inRangeColor]; }
    public static int getOutOfRangeARGB() { return CombatHitboxData.COLOR_ARGB[outOfRangeColor]; }
}
