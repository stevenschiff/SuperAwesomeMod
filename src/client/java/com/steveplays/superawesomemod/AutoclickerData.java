package com.steveplays.superawesomemod;

public final class AutoclickerData {

    public enum Button { LEFT, RIGHT }

    public static final int MIN_CPS = 1;
    public static final int MAX_CPS = 100;

    private static boolean enabled = false;
    private static Button  button  = Button.LEFT;
    private static int     cps     = 10;

    // Tracks elapsed real time between simulated clicks so CPS stays accurate
    // even when client tick rate hiccups.
    private static long lastClickNanos = 0L;

    private AutoclickerData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; lastClickNanos = 0L; }

    public static Button getButton()              { return button; }
    public static void   setButton(Button b)      { button = b; }

    public static int  getCps()        { return cps; }
    public static void setCps(int c)   { cps = Math.clamp(c, MIN_CPS, MAX_CPS); }

    /**
     * Returns the number of clicks that should fire this tick based on real
     * elapsed time. Advances the internal cursor past each click so the
     * average rate stays at exactly {@code cps}. Capped at {@code cps} per
     * call so a stalled tick can't dump a flood of queued clicks.
     */
    public static int clicksToFire(long nowNanos) {
        if (lastClickNanos == 0L) {
            lastClickNanos = nowNanos;
            return 0;
        }
        long intervalNanos = 1_000_000_000L / Math.max(1, cps);
        int fired = 0;
        while (nowNanos - lastClickNanos >= intervalNanos && fired < cps) {
            lastClickNanos += intervalNanos;
            fired++;
        }
        return fired;
    }
}
