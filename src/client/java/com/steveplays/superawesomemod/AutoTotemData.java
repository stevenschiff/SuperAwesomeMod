package com.steveplays.superawesomemod;

public final class AutoTotemData {

    /** Ticks to wait between refill attempts. */
    public static final int MIN_DELAY = 0;
    public static final int MAX_DELAY = 20;

    private static boolean enabled = false;
    private static int delayTicks = 1;

    private AutoTotemData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getDelayTicks()      { return delayTicks; }
    public static void setDelayTicks(int d) { delayTicks = Math.clamp(d, MIN_DELAY, MAX_DELAY); }
}
