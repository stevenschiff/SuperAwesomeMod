package com.steveplays.superawesomemod;

public final class XpMultiplierData {

    /** Multiplier bounds, as shown on the slider. */
    public static final float MIN_MULTIPLIER = 0.1f;
    public static final float MAX_MULTIPLIER = 100.0f;

    private static boolean enabled = false;
    private static float multiplier = 1.0f;

    private XpMultiplierData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static float getMultiplier()        { return multiplier; }
    public static void  setMultiplier(float m) {
        multiplier = Math.clamp(m, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    /**
     * Scales an experience award. Costs (negative amounts, e.g. anvil work) are left
     * alone so the multiplier only ever affects XP coming in.
     */
    public static int apply(int points) {
        if (!enabled || points <= 0) return points;
        return Math.clamp(Math.round((double) points * multiplier), 0, Integer.MAX_VALUE);
    }
}
