package com.steveplays.superawesomemod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player desired entity interaction (attack) range.
 *
 * ConcurrentHashMap is required: in singleplayer the client thread writes
 * this map and the server tick hook reads it. A plain HashMap provides no
 * cross-thread visibility guarantee (Java memory model).
 *
 * The server tick hook reads this value each tick and re-applies it to the
 * attribute, which handles both initial application and any vanilla resets.
 */
public class PlayerAttackRangeData {

    public static final float DEFAULT = 3.0f;  // vanilla survival default

    private static final Map<UUID, Float> ranges = new ConcurrentHashMap<>();

    public static float getRange(UUID uuid) {
        return ranges.getOrDefault(uuid, DEFAULT);
    }

    public static void setRange(UUID uuid, float value) {
        if (value == DEFAULT) {
            ranges.remove(uuid);
        } else {
            ranges.put(uuid, value);
        }
    }

    public static boolean hasCustomRange(UUID uuid) {
        return ranges.containsKey(uuid);
    }

    public static void remove(UUID uuid) {
        ranges.remove(uuid);
    }
}
