package com.steveplays.superawesomemod;

public class ZoomData {

    private static final float DEFAULT_ZOOM = 0.25f;
    private static final float MIN_ZOOM = 0.02f;   // Max zoom in (~50x)
    private static final float MAX_ZOOM = 1.0f;    // No zoom
    private static final float SCROLL_FACTOR = 0.8f; // Each scroll step multiplies/divides by this
    private static final float EASE_PER_TICK = 0.4f;
    private static final float SNAP_EPSILON  = 0.001f;

    private static volatile boolean keyHeld = false;
    private static float zoomTarget = DEFAULT_ZOOM;
    private static float previous = 1.0f;
    private static float current  = 1.0f;

    public static void setKeyHeld(boolean held) { keyHeld = held; }
    public static boolean isKeyHeld()           { return keyHeld; }

    /** Called when scroll wheel moves while zoom key is held. */
    public static void onScroll(double scrollDelta) {
        if (scrollDelta > 0) {
            // Scroll up = zoom in more (smaller multiplier)
            zoomTarget = Math.max(MIN_ZOOM, zoomTarget * SCROLL_FACTOR);
        } else if (scrollDelta < 0) {
            // Scroll down = zoom out (larger multiplier)
            zoomTarget = Math.min(MAX_ZOOM, zoomTarget / SCROLL_FACTOR);
        }
    }

    public static void tick() {
        previous = current;
        float target = keyHeld ? zoomTarget : 1.0f;
        current += (target - current) * EASE_PER_TICK;
        if (Math.abs(target - current) < SNAP_EPSILON) current = target;

        // Reset zoom level when key is released so next press starts at default
        if (!keyHeld) {
            zoomTarget = DEFAULT_ZOOM;
        }
    }

    public static float getMultiplier(float partialTick) {
        return previous + (current - previous) * partialTick;
    }

    public static boolean isActive() { return current < 1.0f || previous < 1.0f; }
}
