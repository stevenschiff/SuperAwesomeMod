package com.steveplays.superawesomemod;

public final class SpacingData {

    /** Band edges, in blocks of gap between your eye and their hitbox. */
    public static final double BAND_INSIDE = 1.0;
    public static final double BAND_CLOSE  = 2.2;
    public static final double BAND_GOOD   = 2.9;

    /** Nothing is drawn unless a player is at least this close. */
    public static final double NEARBY_RADIUS = 8.0;

    public static final int MIN_SCALE = 1;
    public static final int MAX_SCALE = 4;

    /** Where a hit sits relative to the bands. */
    public enum Zone {
        INSIDE("Inside", 0xFFFF5555),
        CLOSE ("Close",  0xFFFFAA00),
        GOOD  ("Good",   0xFF55FF55),
        EDGE  ("Edge",   0xFF55FFFF),
        OUT   ("Out",    0xFF888888);

        public final String label;
        public final int argb;

        Zone(String label, int argb) {
            this.label = label;
            this.argb = argb;
        }
    }

    private static boolean enabled = false;
    private static boolean showMeter = true;
    private static boolean showFlash = true;
    private static boolean gradedCrosshair = false;
    private static int scale = 2;

    private SpacingData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isShowMeter()           { return showMeter; }
    public static void    setShowMeter(boolean v) { showMeter = v; }

    public static boolean isShowFlash()           { return showFlash; }
    public static void    setShowFlash(boolean v) { showFlash = v; }

    public static boolean isGradedCrosshair()           { return gradedCrosshair; }
    public static void    setGradedCrosshair(boolean v) { gradedCrosshair = v; }

    public static int  getScale()      { return scale; }
    public static void setScale(int s) { scale = Math.clamp(s, MIN_SCALE, MAX_SCALE); }

    /**
     * Which band a gap falls in. Anything past the player's own reach is OUT, so a
     * spear's longer reach shifts the bands with it rather than reporting "edge" for
     * a distance that is comfortably hittable.
     */
    public static Zone zoneOf(double gap, double reach) {
        if (gap > reach)        return Zone.OUT;
        if (gap < BAND_INSIDE)  return Zone.INSIDE;
        if (gap < BAND_CLOSE)   return Zone.CLOSE;
        if (gap < BAND_GOOD)    return Zone.GOOD;
        return Zone.EDGE;
    }
}
