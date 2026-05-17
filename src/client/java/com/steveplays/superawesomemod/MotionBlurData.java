package com.steveplays.superawesomemod;

public final class MotionBlurData {

    private MotionBlurData() {}

    private static boolean enabled = false;
    private static int strength = 4; // 1-10

    public static final int MIN_STRENGTH = 1;
    public static final int MAX_STRENGTH = 10;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }

    public static int getStrength() { return strength; }
    public static void setStrength(int v) { strength = Math.max(MIN_STRENGTH, Math.min(MAX_STRENGTH, v)); }

    /** Returns the alpha used to blend the previous frame (0.0 - 1.0). */
    public static float getAlpha() { return strength * 0.1f; }
}
