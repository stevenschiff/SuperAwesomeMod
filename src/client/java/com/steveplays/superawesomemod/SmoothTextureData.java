package com.steveplays.superawesomemod;

/** Settings for temporal frame upscaling of animated textures. */
public final class SmoothTextureData {

    public enum Scope {
        EVERYTHING("Everything"),
        BLOCKS("Blocks only"),
        ITEMS("Items only");

        private final String label;
        Scope(String label) { this.label = label; }
        public String label() { return label; }
        public Scope next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum Mode {
        MOTION("Motion-compensated"),
        CROSSFADE("Cross-fade");

        private final String label;
        Mode(String label) { this.label = label; }
        public String label() { return label; }
        public Mode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private static boolean enabled = false;
    private static Scope scope = Scope.EVERYTHING;
    private static Mode mode = Mode.MOTION;
    private static boolean crispPixels = true;

    private SmoothTextureData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static Scope getScope()        { return scope; }
    public static void  cycleScope()      { scope = scope.next(); }

    public static Mode getMode()          { return mode; }
    public static void cycleMode()        { mode = mode.next(); }

    public static boolean isCrispPixels()           { return crispPixels; }
    public static void    setCrispPixels(boolean c) { crispPixels = c; }
}
