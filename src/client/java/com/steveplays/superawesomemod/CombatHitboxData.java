package com.steveplays.superawesomemod;

public class CombatHitboxData {
    private static boolean enabled = false;
    private static boolean seeThroughWalls = false;
    private static boolean playersOnly = false;
    private static boolean showInvisible = true;
    /** Fill the whole body and armour with the in-range colour instead of only outlining. */
    private static boolean fullColor = false;

    /** Color index for when entity IS in attack range. */
    private static int inRangeColor = 0;   // default: Red
    /** Color index for when entity is NOT in attack range. */
    private static int outOfRangeColor = 7; // default: White

    // Shared color palette — same list used by CombatCrosshairData
    public static final String[] COLOR_NAMES = {
        "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "White", "Black", "Pink", "Magenta"
    };
    public static final int[] COLOR_ARGB = {
        0xFFFF0000, 0xFFFF8800, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
        0xFF0000FF, 0xFF8800FF, 0xFFFFFFFF, 0xFF000000, 0xFFFF88AA, 0xFFFF00FF
    };
    public static final float[][] COLOR_FLOAT = {
        {1.0f, 0.0f, 0.0f},    // Red
        {1.0f, 0.533f, 0.0f},   // Orange
        {1.0f, 1.0f, 0.0f},    // Yellow
        {0.0f, 1.0f, 0.0f},    // Green
        {0.0f, 1.0f, 1.0f},    // Cyan
        {0.0f, 0.0f, 1.0f},    // Blue
        {0.533f, 0.0f, 1.0f},   // Purple
        {1.0f, 1.0f, 1.0f},    // White
        {0.0f, 0.0f, 0.0f},    // Black
        {1.0f, 0.533f, 0.667f}, // Pink
        {1.0f, 0.0f, 1.0f},    // Magenta
    };

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isSeeThroughWalls()           { return seeThroughWalls; }
    public static void    setSeeThroughWalls(boolean v) { seeThroughWalls = v; }

    public static boolean isPlayersOnly()           { return playersOnly; }
    public static void    setPlayersOnly(boolean v) { playersOnly = v; }

    public static boolean isFullColor()           { return fullColor; }
    public static void    setFullColor(boolean v) { fullColor = v; }

    public static boolean isShowInvisible()           { return showInvisible; }
    public static void    setShowInvisible(boolean v) { showInvisible = v; }

    public static int  getInRangeColor()      { return inRangeColor; }
    public static void setInRangeColor(int c)  { inRangeColor = Math.clamp(c, 0, COLOR_NAMES.length - 1); }

    public static int  getOutOfRangeColor()      { return outOfRangeColor; }
    public static void setOutOfRangeColor(int c)  { outOfRangeColor = Math.clamp(c, 0, COLOR_NAMES.length - 1); }

    public static String getInRangeColorName()    { return COLOR_NAMES[inRangeColor]; }
    public static String getOutOfRangeColorName() { return COLOR_NAMES[outOfRangeColor]; }

    public static int getInRangeARGB()    { return COLOR_ARGB[inRangeColor]; }
    public static int getOutOfRangeARGB() { return COLOR_ARGB[outOfRangeColor]; }

    public static float[] getInRangeRGB()    { return COLOR_FLOAT[inRangeColor]; }
    public static float[] getOutOfRangeRGB() { return COLOR_FLOAT[outOfRangeColor]; }
}
