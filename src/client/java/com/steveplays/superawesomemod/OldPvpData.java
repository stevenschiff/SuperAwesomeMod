package com.steveplays.superawesomemod;

public class OldPvpData {
    private static boolean blockingEnabled = false;
    private static boolean swingWhileUsingEnabled = false;
    private static boolean customBlocking = false;

    public static boolean isBlockingEnabled()                 { return blockingEnabled; }
    public static void    setBlockingEnabled(boolean e)       { blockingEnabled = e; }

    public static boolean isSwingWhileUsingEnabled()          { return swingWhileUsingEnabled; }
    public static void    setSwingWhileUsingEnabled(boolean e){ swingWhileUsingEnabled = e; }

    public static boolean isCustomBlocking()                  { return customBlocking; }
    public static void    setCustomBlocking(boolean b)        { customBlocking = b; }
}
