package com.steveplays.superawesomemod;

public final class BoatFlyData {

    private static boolean enabled = false;
    /** Speed in blocks per second, range 1-250. */
    private static int blocksPerSecond = 10;

    private BoatFlyData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getBlocksPerSecond()      { return blocksPerSecond; }
    public static void setBlocksPerSecond(int b) { blocksPerSecond = Math.clamp(b, 1, 250); }

    /**
     * Returns the speed in blocks per tick (20 ticks/sec).
     */
    public static float getSpeed() {
        return blocksPerSecond / 20.0f;
    }
}
