package com.steveplays.superawesomemod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players have mod-granted flight enabled.
 *
 * The server tick loop reads this to re-apply mayfly every tick.
 * This corrects for any vanilla code (respawn, game-mode reload) that
 * silently resets the ability back to false for non-creative players.
 */
public class PlayerFlyData {

    private static final Set<UUID> flyEnabled = ConcurrentHashMap.newKeySet();

    public static boolean isEnabled(UUID uuid) {
        return flyEnabled.contains(uuid);
    }

    public static void setEnabled(UUID uuid, boolean enabled) {
        if (enabled) flyEnabled.add(uuid);
        else         flyEnabled.remove(uuid);
    }

    /** Called on disconnect so the set doesn't grow unbounded. */
    public static void remove(UUID uuid) {
        flyEnabled.remove(uuid);
    }
}
