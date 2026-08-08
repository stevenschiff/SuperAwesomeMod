package com.steveplays.superawesomemod;

public final class FastActionData {

    private static boolean enabled = false;
    private static boolean fastBreak = true;
    private static boolean fastPlace = true;
    private static boolean autoTool = true;

    private FastActionData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isFastBreak()           { return enabled && fastBreak; }
    public static void    setFastBreak(boolean v) { fastBreak = v; }
    public static boolean getFastBreakRaw()       { return fastBreak; }

    public static boolean isFastPlace()           { return enabled && fastPlace; }
    public static void    setFastPlace(boolean v) { fastPlace = v; }
    public static boolean getFastPlaceRaw()       { return fastPlace; }

    public static boolean isAutoTool()           { return enabled && autoTool; }
    public static void    setAutoTool(boolean v) { autoTool = v; }
    public static boolean getAutoToolRaw()       { return autoTool; }
}
