package com.steveplays.superawesomemod;

public final class MinPingData {

    private MinPingData() {}

    private static boolean enabled = false;
    private static int minPingMs = 0;

    public static final int MIN_PING = 0;
    public static final int MAX_PING = 500;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }

    public static int getMinPingMs() { return minPingMs; }
    public static void setMinPingMs(int v) { minPingMs = Math.max(MIN_PING, Math.min(MAX_PING, v)); }
}
