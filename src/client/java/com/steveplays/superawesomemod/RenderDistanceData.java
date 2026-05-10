package com.steveplays.superawesomemod;

public final class RenderDistanceData {

    public static final int MIN_DISTANCE = 32;
    public static final int MAX_DISTANCE = 128;

    private static boolean enabled  = false;
    private static int     distance = MIN_DISTANCE;

    private RenderDistanceData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getDistance()      { return distance; }
    public static void setDistance(int d) { distance = Math.clamp(d, MIN_DISTANCE, MAX_DISTANCE); }
}
