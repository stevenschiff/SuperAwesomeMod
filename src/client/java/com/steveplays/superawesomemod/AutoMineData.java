package com.steveplays.superawesomemod;

import net.minecraft.world.level.block.Block;

public final class AutoMineData {

    public static final int MIN_RADIUS = 8;
    public static final int MAX_RADIUS = 48;

    private static boolean enabled = false;
    private static boolean running = false;
    private static int searchRadius = 24;

    /** The block type marked with the mark key, or null if nothing is marked. */
    private static Block target = null;
    /** Shown on the HUD so a stall is visible rather than silent. */
    private static String status = "Idle";

    private AutoMineData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) {
        enabled = e;
        if (!e) stop("Idle");
    }

    /** True only while actively walking and mining. The keys are inert unless enabled. */
    public static boolean isRunning() { return enabled && running; }

    public static void start() {
        if (enabled && target != null) {
            running = true;
            status = "Searching";
        }
    }

    public static void stop(String why) {
        running = false;
        status = why;
    }

    public static int  getSearchRadius()      { return searchRadius; }
    public static void setSearchRadius(int r) { searchRadius = Math.clamp(r, MIN_RADIUS, MAX_RADIUS); }

    public static Block getTarget()          { return target; }
    public static void  setTarget(Block b)   { target = b; }
    public static boolean hasTarget()        { return target != null; }

    public static String getStatus()           { return status; }
    public static void   setStatus(String s)   { status = s; }

    public static String getTargetName() {
        return target == null ? "none" : target.getName().getString();
    }
}
