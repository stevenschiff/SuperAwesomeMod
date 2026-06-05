package com.steveplays.superawesomemod;

public final class KeystrokesData {
    private static boolean enabled = false;
    private static int     scale   = 5;  // 1-10, default 5
    private static int     corner  = 1;  // 0=TL, 1=TR, 2=BL, 3=BR

    private KeystrokesData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getScale()      { return scale; }
    public static void setScale(int s) { scale = Math.clamp(s, 1, 10); }

    public static int  getCorner()      { return corner; }
    public static void setCorner(int c) { corner = Math.clamp(c, 0, 3); }

    public static String getCornerName() {
        return switch (corner) {
            case 0 -> "Top-Left";
            case 1 -> "Top-Right";
            case 2 -> "Bottom-Left";
            case 3 -> "Bottom-Right";
            default -> "Top-Right";
        };
    }
}
