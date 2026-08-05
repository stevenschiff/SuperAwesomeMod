package com.steveplays.superawesomemod;

public final class FlightData {

    /** Lowest / highest speed the input box accepts, in blocks per second. */
    public static final int MIN_BLOCKS_PER_SECOND = 1;
    public static final int MAX_BLOCKS_PER_SECOND = 10000;

    private static boolean enabled = false;
    /** Speed in blocks per second, range 1-10000. */
    private static int blocksPerSecond = 10;

    private FlightData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getBlocksPerSecond()      { return blocksPerSecond; }
    public static void setBlocksPerSecond(int b) {
        blocksPerSecond = Math.clamp(b, MIN_BLOCKS_PER_SECOND, MAX_BLOCKS_PER_SECOND);
    }

    /**
     * Returns the speed in blocks per tick (20 ticks/sec).
     * E.g. 50 blocks/sec → 2.5 blocks/tick.
     */
    public static float getSpeed() {
        return blocksPerSecond / 20.0f;
    }
}
