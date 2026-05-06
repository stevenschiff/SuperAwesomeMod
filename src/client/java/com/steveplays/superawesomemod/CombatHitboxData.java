package com.steveplays.superawesomemod;

public class CombatHitboxData {
    private static boolean enabled = false;
    private static boolean seeThroughWalls = false;
    private static boolean playersOnly = false;
    private static boolean showInvisible = true;

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isSeeThroughWalls()           { return seeThroughWalls; }
    public static void    setSeeThroughWalls(boolean v) { seeThroughWalls = v; }

    public static boolean isPlayersOnly()           { return playersOnly; }
    public static void    setPlayersOnly(boolean v) { playersOnly = v; }

    public static boolean isShowInvisible()           { return showInvisible; }
    public static void    setShowInvisible(boolean v) { showInvisible = v; }
}
