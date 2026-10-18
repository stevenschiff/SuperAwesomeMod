package com.steveplays.superawesomemod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Sends a macro's line when its key is pressed.
 *
 * <p>Two modes, per slot-wide setting:
 * <ul>
 *   <li><b>Repeat off</b> — fires on the <em>rising edge</em> of the key, the tick it goes
 *       from up to down, so holding sends exactly once however long it is held.</li>
 *   <li><b>Repeat on</b> — keeps firing while the key stays down, one send per cooldown
 *       interval.</li>
 * </ul>
 *
 * <p>Either way the cooldown is the floor on send rate. Minecraft's own {@code consumeClick}
 * queue is drained rather than used as the trigger, because queued presses would otherwise
 * fire in a burst after a pause.
 *
 * <p>Nothing is sent while a screen is open, so typing a macro into its own settings box
 * cannot trigger it.
 */
public final class ChatMacroHandler {

    private static final boolean[] wasDown = new boolean[ChatMacroData.SLOTS];
    private static final long[] lastSentMs = new long[ChatMacroData.SLOTS];

    private ChatMacroHandler() {}

    public static void tick(Minecraft client) {
        KeyMapping[] keys = ModKeybindings.chatMacros;
        if (keys == null) return;

        boolean repeat = ChatMacroData.isRepeatWhileHeld();

        for (int i = 0; i < ChatMacroData.SLOTS && i < keys.length; i++) {
            // Always drain, enabled or not, so presses never bank up.
            while (keys[i].consumeClick()) { /* drained deliberately */ }

            boolean down = keys[i].isDown();
            boolean rising = down && !wasDown[i];
            wasDown[i] = down;

            boolean wants = repeat ? down : rising;
            if (!wants) continue;
            if (!ChatMacroData.isEnabled()) continue;
            if (client.screen != null || client.player == null) continue;

            long now = System.currentTimeMillis();
            if (now - lastSentMs[i] < ChatMacroData.getCooldownMs()) continue;

            String message = ChatMacroData.getMessage(i).trim();
            if (message.isEmpty()) continue;

            lastSentMs[i] = now;
            send(client, message);
        }
    }

    private static void send(Minecraft client, String message) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return;

        if (message.startsWith("/")) {
            // sendCommand expects the command without its slash.
            connection.sendCommand(message.substring(1));
        } else {
            connection.sendChat(message);
        }
    }
}
