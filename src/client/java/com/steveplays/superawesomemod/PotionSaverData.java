package com.steveplays.superawesomemod;

public final class PotionSaverData {

    private static boolean enabled = false;

    /**
     * Set for the duration of one entity's effect tick so the effect instances being
     * ticked know they belong to us. Per-thread because in singleplayer the client
     * thread and the integrated server thread both tick effects, on their own copies
     * of the world, at the same time.
     */
    private static final ThreadLocal<Boolean> HOLDING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private PotionSaverData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isHolding()           { return HOLDING.get(); }
    public static void    setHolding(boolean h) { HOLDING.set(h); }
}
