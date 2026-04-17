package com.steveplays.superawesomemod;

public class FreeLookData {
    private static float yawOffset = 0f;
    private static float pitchOffset = 0f;
    private static boolean toggleMode = false;
    private static boolean active = false;

    public static void addDelta(float yaw, float pitch) {
        yawOffset += yaw;
        pitchOffset = Math.clamp(pitchOffset + pitch, -90f, 90f);
    }

    public static float getYawOffset()   { return yawOffset; }
    public static float getPitchOffset() { return pitchOffset; }

    public static void reset() {
        yawOffset = 0f;
        pitchOffset = 0f;
    }

    public static boolean isActive()            { return active; }
    public static void    setActive(boolean a)  { active = a; }

    public static boolean isToggleMode()               { return toggleMode; }
    public static void    setToggleMode(boolean mode)  { toggleMode = mode; }
}
