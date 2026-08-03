package com.steveplays.superawesomemod;

public final class FlightData {

    public static final float SLOW      = 0.15f;
    public static final float NORMAL    = 0.40f;
    public static final float FAST      = 1.00f;
    public static final float VERY_FAST = 2.00f;

    private static boolean enabled = false;
    private static float   speed   = NORMAL;

    private FlightData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static float getSpeed()           { return speed; }
    public static void  setSpeed(float s)    { speed = s; }
}
