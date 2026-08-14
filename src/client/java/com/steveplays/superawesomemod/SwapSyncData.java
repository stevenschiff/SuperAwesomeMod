package com.steveplays.superawesomemod;

public final class SwapSyncData {

    /** Ticks to hold an attack after a hotbar swap. */
    public static final int MIN_GUARD_TICKS = 1;
    public static final int MAX_GUARD_TICKS = 5;

    private static boolean enabled = false;
    private static int guardTicks = 1;

    private SwapSyncData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getGuardTicks()      { return guardTicks; }
    public static void setGuardTicks(int t) {
        guardTicks = Math.clamp(t, MIN_GUARD_TICKS, MAX_GUARD_TICKS);
    }
}
