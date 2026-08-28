package com.steveplays.superawesomemod;

public final class AntiHungerData {

    public static final int MIN_PAUSE_RADIUS = 3;
    public static final int MAX_PAUSE_RADIUS = 24;

    private static boolean enabled = false;
    private static boolean walkReport = true;
    private static boolean freezeHunger = true;
    private static boolean pauseNearPlayers = true;
    private static int pauseRadius = 8;

    private AntiHungerData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    /** Report walking to the server while we sprint. Works on any server. */
    public static boolean isWalkReport()           { return walkReport; }
    public static void    setWalkReport(boolean v) { walkReport = v; }

    /** Cancel exhaustion outright. Only real in a world we host. */
    public static boolean isFreezeHunger()           { return freezeHunger; }
    public static void    setFreezeHunger(boolean v) { freezeHunger = v; }

    /**
     * Report honestly while an opponent is close. Sprint-hit knockback is resolved
     * server-side from the sprint flag, so spoofing costs it — worth giving back
     * during a fight and reclaiming everywhere else.
     */
    public static boolean isPauseNearPlayers()           { return pauseNearPlayers; }
    public static void    setPauseNearPlayers(boolean v) { pauseNearPlayers = v; }

    public static int  getPauseRadius()      { return pauseRadius; }
    public static void setPauseRadius(int r) {
        pauseRadius = Math.clamp(r, MIN_PAUSE_RADIUS, MAX_PAUSE_RADIUS);
    }

    public static boolean isWalkReportActive() {
        return enabled && walkReport;
    }

    public static boolean isFreezeHungerActive() {
        return enabled && freezeHunger;
    }
}
