package com.steveplays.superawesomemod;

public class FreeLookData {
    private static float yawOffset = 0f;
    private static float pitchOffset = 0f;
    private static boolean enabled = false; // toggled via menu
    private static boolean active  = false; // enabled AND currently in third-person

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

    public static boolean isEnabled()          { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static boolean isActive()           { return active; }
    public static void    setActive(boolean a) { active = a; }
}
