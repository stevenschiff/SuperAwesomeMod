package com.steveplays.superawesomemod;

import net.minecraft.world.phys.Vec3;

public final class AntiKnockbackData {

    /** Percent of incoming knockback actually applied. 0 = immune, 100 = vanilla. */
    public static final int MIN_PERCENT = 0;
    public static final int MAX_PERCENT = 100;

    private static boolean enabled = false;
    private static int percent = 0;

    private AntiKnockbackData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getPercent()      { return percent; }
    public static void setPercent(int p) { percent = Math.clamp(p, MIN_PERCENT, MAX_PERCENT); }

    /** Scales a knockback vector down to the configured share. */
    public static Vec3 scale(Vec3 knockback) {
        if (!enabled) return knockback;
        if (percent == 0) return Vec3.ZERO;
        return knockback.scale(percent / 100.0);
    }
}
