package com.steveplays.superawesomemod;

public final class SlamOptimizerData {

    /** Upper bound on the ping-scaled settle, in ticks. */
    public static final int MIN_MAX_WAIT = 1;
    public static final int MAX_MAX_WAIT = 6;

    /** How long after a combo swap the charge gate keeps applying. */
    public static final int COMBO_WINDOW_TICKS = 20;

    private static boolean enabled = false;
    private static int maxWaitTicks = 4;

    private SlamOptimizerData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getMaxWaitTicks()      { return maxWaitTicks; }
    public static void setMaxWaitTicks(int t) {
        maxWaitTicks = Math.clamp(t, MIN_MAX_WAIT, MAX_MAX_WAIT);
    }

    /**
     * Settle length for a combo swap: one tick so the swap and the hit can't land in
     * the same server tick, plus one more per 100ms of ping as a hedge against jitter.
     */
    public static int settleTicksFor(int pingMs) {
        int scaled = 1 + Math.max(0, pingMs) / 100;
        return Math.clamp(scaled, MIN_MAX_WAIT, maxWaitTicks);
    }
}
