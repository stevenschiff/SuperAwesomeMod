package com.steveplays.superawesomemod;

public final class AutoFishData {

    /** Stop casting once the rod has this much durability left or less. */
    public static final int MIN_RESERVE = 1;
    public static final int MAX_RESERVE = 50;

    private static boolean enabled = false;
    private static int durabilityReserve = 10;
    private static String status = "Idle";

    private AutoFishData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) {
        enabled = e;
        status = e ? "Waiting" : "Idle";
    }

    public static int  getDurabilityReserve()      { return durabilityReserve; }
    public static void setDurabilityReserve(int r) {
        durabilityReserve = Math.clamp(r, MIN_RESERVE, MAX_RESERVE);
    }

    public static String getStatus()         { return status; }
    public static void   setStatus(String s) { status = s; }
}
