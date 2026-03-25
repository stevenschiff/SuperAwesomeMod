package com.steveplays.superawesomemod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-player jump height multipliers.
 * Data lives in memory only — resets when the server restarts.
 *
 * ConcurrentHashMap is required: in singleplayer the server thread writes
 * this map and the client thread reads it inside JumpMixin. A plain HashMap
 * provides no cross-thread visibility guarantee (Java memory model), so the
 * client thread would see a permanently stale value from its CPU cache.
 */
public class PlayerJumpData {

    public static final float DEFAULT = 1.0f;
    public static final float MIN = 0.1f;
    public static final float MAX = 10.0f;

    private static final Map<UUID, Float> multipliers = new ConcurrentHashMap<>();

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
