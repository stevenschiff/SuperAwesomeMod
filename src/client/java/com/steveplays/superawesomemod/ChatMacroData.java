package com.steveplays.superawesomemod;

/**
 * Five macro slots, each a line of text bound to its own key.
 *
 * <p>Slots are fixed rather than growable because Fabric keybinds must be registered at
 * startup — a macro added at runtime would have no key to bind to.
 */
public final class ChatMacroData {

    public static final int SLOTS = 5;

    /** Shortest gap between two sends from the same slot. One tick is 50 ms. */
    public static final int MIN_COOLDOWN_MS = 50;
    public static final int MAX_COOLDOWN_MS = 3000;

    private static boolean enabled = false;
    private static final String[] messages = new String[SLOTS];
    private static int cooldownMs = 500;
    private static boolean repeatWhileHeld = false;

    static {
        for (int i = 0; i < SLOTS; i++) messages[i] = "";
    }

    private ChatMacroData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static String getMessage(int slot) {
        return (slot < 0 || slot >= SLOTS) ? "" : messages[slot];
    }

    public static void setMessage(int slot, String text) {
        if (slot < 0 || slot >= SLOTS) return;
        // Chat has a hard length limit; trimming here avoids a rejected packet later.
        messages[slot] = text.length() > 256 ? text.substring(0, 256) : text;
    }

    public static boolean isRepeatWhileHeld()           { return repeatWhileHeld; }
    public static void    setRepeatWhileHeld(boolean r) { repeatWhileHeld = r; }

    public static int  getCooldownMs()      { return cooldownMs; }
    public static void setCooldownMs(int m) {
        cooldownMs = Math.clamp(m, MIN_COOLDOWN_MS, MAX_COOLDOWN_MS);
    }
}
