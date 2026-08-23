package com.steveplays.superawesomemod;

public final class ShieldData {

    /** 0 = fully solid, 100 = fully invisible. */
    public static final int MIN_TRANSPARENCY = 0;
    public static final int MAX_TRANSPARENCY = 100;

    /** Offsets are stored in hundredths so the slider can be whole numbers. */
    public static final int MIN_OFFSET = -50;
    public static final int MAX_OFFSET = 50;

    private static boolean enabled = false;
    private static int transparency = 0;
    private static int usedOffset = 0;
    private static int notUsedOffset = 0;

    private ShieldData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getTransparency()      { return transparency; }
    public static void setTransparency(int t) {
        transparency = Math.clamp(t, MIN_TRANSPARENCY, MAX_TRANSPARENCY);
    }

    /** Y offset applied while the shield is raised (right-click held). */
    public static int  getUsedOffset()      { return usedOffset; }
    public static void setUsedOffset(int o) { usedOffset = Math.clamp(o, MIN_OFFSET, MAX_OFFSET); }

    /** Y offset applied while the shield is merely held. */
    public static int  getNotUsedOffset()      { return notUsedOffset; }
    public static void setNotUsedOffset(int o) { notUsedOffset = Math.clamp(o, MIN_OFFSET, MAX_OFFSET); }

    public static float offsetFor(boolean blocking) {
        return (blocking ? usedOffset : notUsedOffset) / 100.0f;
    }

    /**
     * Alpha byte for the shield tint. Never returns 0 for a shield we still intend to
     * draw: a fully transparent shield is skipped outright instead, because a zero
     * colour is treated as "no tint" further down rather than as invisible.
     */
    public static int alpha() {
        int a = Math.round(255f * (1f - transparency / 100f));
        return Math.clamp(a, 0, 255);
    }

    /** True when the shield should be drawn at all. */
    public static boolean isVisible() {
        return transparency < MAX_TRANSPARENCY;
    }

    /** Whether anything should be altered at render time. */
    public static boolean isTintActive() {
        return enabled && transparency > MIN_TRANSPARENCY;
    }
}
