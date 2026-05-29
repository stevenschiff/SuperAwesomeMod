package com.steveplays.superawesomemod;

/**
 * Static state singleton for the schematic feature.
 * Follows the same pattern as {@link MiniMapData} and {@link XrayData}.
 */
public final class SchematicData {

    private SchematicData() {}

    // Master toggle
    private static boolean enabled = false;

    // Currently loaded placement (schematic + world position + rotation)
    private static SchematicPlacement currentPlacement = null;

    // Layer-by-layer mode
    private static boolean layerMode = false;
    private static int currentLayer = 0;

    // Render mode: 0 = ghost overlay, 1 = verifier
    private static int renderMode = 0;

    // Ghost overlay transparency (0.0 - 1.0)
    private static float ghostAlpha = 0.3f;

    // Easy Place master toggle (visual indicator)
    private static boolean easyPlaceEnabled = false;

    // Easy Place "Allow on Server" toggle (actual auto-placement)
    private static boolean easyPlaceAllowOnServer = false;

    // --- Master toggle ---
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean e) { enabled = e; }

    // --- Current placement ---
    public static SchematicPlacement getCurrentPlacement() { return currentPlacement; }
    public static void setCurrentPlacement(SchematicPlacement p) { currentPlacement = p; }

    // --- Layer mode ---
    public static boolean isLayerMode() { return layerMode; }
    public static void setLayerMode(boolean m) { layerMode = m; }

    public static int getCurrentLayer() { return currentLayer; }
    public static void setCurrentLayer(int layer) { currentLayer = Math.max(0, layer); }

    // --- Render mode ---
    public static int getRenderMode() { return renderMode; }
    public static void setRenderMode(int mode) { renderMode = Math.max(0, Math.min(1, mode)); }
    public static void cycleRenderMode() { renderMode = (renderMode + 1) % 2; }

    public static String getRenderModeName() {
        return switch (renderMode) {
            case 0 -> "Ghost";
            case 1 -> "Verifier";
            default -> "Ghost";
        };
    }

    // --- Ghost alpha ---
    public static float getGhostAlpha() { return ghostAlpha; }
    public static void setGhostAlpha(float a) { ghostAlpha = Math.max(0.1f, Math.min(1.0f, a)); }

    private static final float[] ALPHA_STEPS = {0.1f, 0.2f, 0.3f, 0.5f, 0.7f};
    public static void cycleGhostAlpha() {
        for (int i = 0; i < ALPHA_STEPS.length; i++) {
            if (Math.abs(ghostAlpha - ALPHA_STEPS[i]) < 0.01f) {
                ghostAlpha = ALPHA_STEPS[(i + 1) % ALPHA_STEPS.length];
                return;
            }
        }
        ghostAlpha = ALPHA_STEPS[0];
    }

    // --- Easy Place ---
    public static boolean isEasyPlaceEnabled() { return easyPlaceEnabled; }
    public static void setEasyPlaceEnabled(boolean e) { easyPlaceEnabled = e; }

    public static boolean isEasyPlaceAllowOnServer() { return easyPlaceAllowOnServer; }
    public static void setEasyPlaceAllowOnServer(boolean a) { easyPlaceAllowOnServer = a; }

    /** True when auto-placement should actively modify block placement. */
    public static boolean isAutoPlaceActive() {
        return easyPlaceEnabled && easyPlaceAllowOnServer;
    }

    /**
     * Returns the maximum Y layer for the current placement, or 0 if none loaded.
     */
    public static int getMaxLayer() {
        if (currentPlacement == null) return 0;
        var min = currentPlacement.getMin();
        var max = currentPlacement.getMax();
        return max.getY() - min.getY() - 1;
    }
}
