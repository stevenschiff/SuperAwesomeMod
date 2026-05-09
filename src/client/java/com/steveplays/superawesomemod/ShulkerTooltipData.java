package com.steveplays.superawesomemod;

public final class ShulkerTooltipData {

    public static final int MIN_SCALE = 1;
    public static final int MAX_SCALE = 10;

    private static boolean enabled = false;
    private static int     scale   = 5;

    private ShulkerTooltipData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getScale()      { return scale; }
    public static void setScale(int s) { scale = Math.clamp(s, MIN_SCALE, MAX_SCALE); }
}
