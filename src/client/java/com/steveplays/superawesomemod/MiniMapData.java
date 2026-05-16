package com.steveplays.superawesomemod;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MiniMapData {

    private MiniMapData() {}

    // Master toggle (feature on/off from G menu)
    private static boolean enabled = false;

    // Corner HUD minimap toggle (always visible during gameplay when true)
    private static boolean hudVisible = true;

    // HUD minimap size in pixels (64-256)
    private static int minimapSize = 128;

    // Corner position: 0=TL, 1=TR, 2=BL, 3=BR
    private static int corner = 1;

    // Waypoints (thread-safe for render access)
    private static final CopyOnWriteArrayList<MiniMapWaypoint> waypoints = new CopyOnWriteArrayList<>();

    // World identification for persistence
    private static String currentWorldId = "";

    // --- Master toggle ---
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean e) { enabled = e; }

    // --- HUD visibility ---
    public static boolean isHudVisible() { return hudVisible; }
    public static void setHudVisible(boolean v) { hudVisible = v; }

    // --- Minimap size ---
    public static int getMinimapSize() { return minimapSize; }
    public static void setMinimapSize(int size) {
        minimapSize = Math.max(64, Math.min(256, size));
    }

    // --- Corner ---
    public static int getCorner() { return corner; }
    public static void setCorner(int c) { corner = Math.max(0, Math.min(3, c)); }

    public static String getCornerName() {
        return switch (corner) {
            case 0 -> "Top-Left";
            case 1 -> "Top-Right";
            case 2 -> "Bottom-Left";
            case 3 -> "Bottom-Right";
            default -> "Top-Right";
        };
    }

    // --- Waypoints ---
    public static List<MiniMapWaypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public static void addWaypoint(MiniMapWaypoint wp) {
        waypoints.add(wp);
    }

    public static void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
        }
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static void setWaypoints(List<MiniMapWaypoint> wps) {
        waypoints.clear();
        waypoints.addAll(wps);
    }

    // --- World ID ---
    public static String getCurrentWorldId() { return currentWorldId; }
    public static void setCurrentWorldId(String id) { currentWorldId = id; }
}
