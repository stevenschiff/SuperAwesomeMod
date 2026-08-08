package com.steveplays.superawesomemod;

public final class StunSlamData {

    /** How the clicks get fired. */
    public enum Mode { SWAP_ASSIST, MACRO }

    public static final int MIN_CLICKS = 1;
    public static final int MAX_CLICKS = 5;

    private static boolean enabled = false;
    private static Mode mode = Mode.SWAP_ASSIST;

    /** Clicks fired when swapping spear -> axe. */
    private static int axeClicks = 1;
    /** Clicks fired when swapping axe -> mace. */
    private static int maceClicks = 2;

    private StunSlamData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static Mode getMode()          { return mode; }
    public static void setMode(Mode m)    { mode = m; }
    public static void cycleMode() {
        mode = (mode == Mode.SWAP_ASSIST) ? Mode.MACRO : Mode.SWAP_ASSIST;
    }
    public static String getModeName() {
        return mode == Mode.SWAP_ASSIST ? "Swap Assist" : "Macro (keybind)";
    }

    public static int  getAxeClicks()      { return axeClicks; }
    public static void setAxeClicks(int c) { axeClicks = Math.clamp(c, MIN_CLICKS, MAX_CLICKS); }

    public static int  getMaceClicks()      { return maceClicks; }
    public static void setMaceClicks(int c) { maceClicks = Math.clamp(c, MIN_CLICKS, MAX_CLICKS); }
}
