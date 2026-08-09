package com.steveplays.superawesomemod;

public final class BHopData {

    /** Speed range in blocks per second. */
    public static final int MIN_BLOCKS_PER_SECOND = 1;
    public static final int MAX_BLOCKS_PER_SECOND = 500;

    /**
     * Above roughly this speed a vanilla server's "moved too quickly" check fires and
     * teleports us back — it allows about 10 blocks per tick. Only used to colour the
     * warning in the menu; the slider still goes to the full 500.
     */
    public static final int SERVER_SAFE_LIMIT = 200;

    private static boolean enabled = false;
    private static int blocksPerSecond = 20;

    private BHopData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getBlocksPerSecond()      { return blocksPerSecond; }
    public static void setBlocksPerSecond(int b) {
        blocksPerSecond = Math.clamp(b, MIN_BLOCKS_PER_SECOND, MAX_BLOCKS_PER_SECOND);
    }

    /** Speed in blocks per tick, which is what delta movement is measured in. */
    public static float getSpeed() {
        return blocksPerSecond / 20.0f;
    }
}
