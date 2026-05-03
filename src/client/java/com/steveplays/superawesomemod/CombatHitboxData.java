package com.steveplays.superawesomemod;

public class CombatHitboxData {
    private static boolean enabled = false;
    private static boolean seeThroughWalls = false;

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isSeeThroughWalls()           { return seeThroughWalls; }
    public static void    setSeeThroughWalls(boolean v) { seeThroughWalls = v; }
}
