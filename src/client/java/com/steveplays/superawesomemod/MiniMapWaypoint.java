package com.steveplays.superawesomemod;

public record MiniMapWaypoint(String name, int x, int z, int color, boolean visible, String dimension) {

    public MiniMapWaypoint(String name, int x, int z, int color) {
        this(name, x, z, color, true, "");
    }

    public MiniMapWaypoint(String name, int x, int z, int color, boolean visible) {
        this(name, x, z, color, visible, "");
    }

    public MiniMapWaypoint withVisible(boolean v) {
        return new MiniMapWaypoint(name, x, z, color, v, dimension);
    }

    public float r() { return ((color >> 16) & 0xFF) / 255f; }
    public float g() { return ((color >> 8) & 0xFF) / 255f; }
    public float b() { return (color & 0xFF) / 255f; }
}
