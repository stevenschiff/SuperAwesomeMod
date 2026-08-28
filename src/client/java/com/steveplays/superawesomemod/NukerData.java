package com.steveplays.superawesomemod;

public final class NukerData {

    /** Radius in blocks. Capped by the player's block reach at use time regardless. */
    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 5;

    private static boolean enabled = false;
    private static int radius = 3;
    private static boolean instantOnly = true;

    private NukerData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getRadius()      { return radius; }
    public static void setRadius(int r) { radius = Math.clamp(r, MIN_RADIUS, MAX_RADIUS); }

    /**
     * Only fire at blocks the server will insta-mine. With this off, harder blocks are
     * also chewed through — but one at a time, because the server tracks a single
     * in-progress block.
     */
    public static boolean isInstantOnly()           { return instantOnly; }
    public static void    setInstantOnly(boolean v) { instantOnly = v; }
}
