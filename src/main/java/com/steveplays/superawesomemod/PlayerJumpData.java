package com.steveplays.superawesomemod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores per-player jump height multipliers.
 * Data lives in memory only — resets when the server restarts.
 */
public class PlayerJumpData {

    public static final float DEFAULT = 1.0f;
    public static final float MIN = 0.1f;
    public static final float MAX = 10.0f;

    private static final Map<UUID, Float> multipliers = new HashMap<>();

    public static float getMultiplier(UUID uuid) {
        return multipliers.getOrDefault(uuid, DEFAULT);
    }

    public static void setMultiplier(UUID uuid, float value) {
        if (value == DEFAULT) {
            multipliers.remove(uuid);
        } else {
            multipliers.put(uuid, Math.clamp(value, MIN, MAX));
        }
    }
}
