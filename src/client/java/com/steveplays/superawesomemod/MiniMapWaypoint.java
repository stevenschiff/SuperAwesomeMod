package com.steveplays.superawesomemod;

public record MiniMapWaypoint(String name, int x, int z, int color) {

    public float r() { return ((color >> 16) & 0xFF) / 255f; }
    public float g() { return ((color >> 8) & 0xFF) / 255f; }
    public float b() { return (color & 0xFF) / 255f; }
}
