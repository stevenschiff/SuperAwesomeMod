package com.steveplays.superawesomemod;

public final class MaceTimingData {

    /** Minimum attack-strength charge, as a percent, before a mace swing is allowed. */
    public static final int MIN_STRENGTH = 50;
    public static final int MAX_STRENGTH = 100;

    private static boolean enabled = false;
    private static int minStrengthPercent = 100;

    private MaceTimingData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getMinStrengthPercent()      { return minStrengthPercent; }
    public static void setMinStrengthPercent(int p) {
        minStrengthPercent = Math.clamp(p, MIN_STRENGTH, MAX_STRENGTH);
    }

    public static float getMinStrength() {
        return minStrengthPercent / 100.0f;
    }
}
