package com.steveplays.superawesomemod;

public class ArmorHudData {
    private static boolean enabled = false;
    /** Scale factor 0.5–5.0 in 0.1 increments. */
    private static float scale = 1.0f;
    /** Text scale factor 0.5–3.0 in 0.1 increments. */
    private static float textScale = 1.0f;
    /** Pixel offset above the armor icon for the durability number (0–30). */
    private static int durabilityHeight = 10;

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static float getScale()       { return scale; }
    public static void  setScale(float s) { scale = Math.max(0.5f, Math.min(s, 5.0f)); }

    public static float getTextScale()       { return textScale; }
    public static void  setTextScale(float s) { textScale = Math.max(0.5f, Math.min(s, 3.0f)); }

    public static int  getDurabilityHeight()      { return durabilityHeight; }
    public static void setDurabilityHeight(int h)  { durabilityHeight = Math.max(0, Math.min(h, 30)); }
}
