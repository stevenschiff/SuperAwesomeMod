package com.steveplays.superawesomemod;

public class ZoomData {

    public static final float ZOOM_MULTIPLIER = 0.25f;
    private static final float EASE_PER_TICK = 0.4f;
    private static final float SNAP_EPSILON  = 0.001f;

    private static volatile boolean keyHeld = false;
    private static float previous = 1.0f;
    private static float current  = 1.0f;

    public static void setKeyHeld(boolean held) { keyHeld = held; }
    public static boolean isKeyHeld()           { return keyHeld; }

    public static void tick() {
        previous = current;
        float target = keyHeld ? ZOOM_MULTIPLIER : 1.0f;
        current += (target - current) * EASE_PER_TICK;
        if (Math.abs(target - current) < SNAP_EPSILON) current = target;
    }

    public static float getMultiplier(float partialTick) {
        return previous + (current - previous) * partialTick;
    }

    public static boolean isActive() { return current < 1.0f || previous < 1.0f; }
}
