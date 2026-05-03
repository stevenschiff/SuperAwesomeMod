package com.steveplays.superawesomemod;

public class ZoomData {

    public static final float ZOOM_MULTIPLIER = 0.25f;
    private static final float STEP_PER_TICK  = 0.15f;

    private static volatile boolean keyHeld = false;
    private static float current = 1.0f;

    public static void setKeyHeld(boolean held) { keyHeld = held; }
    public static boolean isKeyHeld()           { return keyHeld; }

    public static void tick() {
        float target = keyHeld ? ZOOM_MULTIPLIER : 1.0f;
        if (current < target)      current = Math.min(target, current + STEP_PER_TICK);
        else if (current > target) current = Math.max(target, current - STEP_PER_TICK);
    }

    public static float getMultiplier() { return current; }

    public static boolean isActive() { return current < 1.0f; }
}
