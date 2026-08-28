package com.steveplays.superawesomemod;

public final class AutoEatData {

    /** Hunger level (out of 20) at or below which we start eating. */
    public static final int MIN_THRESHOLD = 1;
    public static final int MAX_THRESHOLD = 19;

    private static boolean enabled = false;
    private static int threshold = 16;

    private AutoEatData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getThreshold()      { return threshold; }
    public static void setThreshold(int t) { threshold = Math.clamp(t, MIN_THRESHOLD, MAX_THRESHOLD); }
}
