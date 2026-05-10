package com.steveplays.superawesomemod;

public final class RenderDistanceData {

    public static final int MIN_DISTANCE = 2;
    public static final int MAX_DISTANCE = 256;

    private static boolean enabled  = false;
    private static int     distance = MIN_DISTANCE;
    private static long    seed     = 0;
    private static boolean seedSet  = false;

    private RenderDistanceData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getDistance()      { return distance; }
    public static void setDistance(int d) { distance = Math.clamp(d, MIN_DISTANCE, MAX_DISTANCE); }

    public static long    getSeed()        { return seed; }
    public static boolean isSeedSet()      { return seedSet; }
    public static void    setSeed(long s)  { seed = s; seedSet = true; }
    public static void    clearSeed()      { seed = 0; seedSet = false; }
}
