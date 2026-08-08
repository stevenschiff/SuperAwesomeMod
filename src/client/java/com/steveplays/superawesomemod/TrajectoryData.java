package com.steveplays.superawesomemod;

public final class TrajectoryData {

    private static boolean enabled = false;

    private TrajectoryData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }
}
