package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.Player;

/**
 * Owns what the server believes about our sprinting.
 *
 * <p>The server charges hunger for distance only when it thinks we are sprinting —
 * {@code ServerPlayer.checkMovementStatistics} bills {@code 0.1F} per metre sprinting
 * and nothing at all walking — and the sprint flag is something the client reports
 * rather than something the server observes. Reporting a walk while actually
 * sprinting therefore makes overland travel free, on any server.
 *
 * <p>Rather than filtering vanilla's sprint packets and hoping its bookkeeping stays
 * consistent, this takes the state over completely: the mixin drops every sprint
 * packet the client generates, and this class sends exactly the ones it wants. Vanilla
 * only transmits on a change, so a dropped packet would otherwise leave the server
 * permanently out of step with no way to correct it — including when we deliberately
 * want to tell the truth again.
 */
public final class AntiHungerHandler {

    /** Open while we send our own sprint packet, so the mixin lets that one through. */
    private static boolean sendingOwn = false;

    /** What we believe the server currently thinks. */
    private static boolean serverThinksSprinting = false;
    private static boolean tracking = false;

    private AntiHungerHandler() {}

    public static boolean isSendingOwn() {
        return sendingOwn;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.getConnection() == null) {
            tracking = false;
            return;
        }

        boolean spoof = AntiHungerData.isWalkReportActive() && !nearAnyPlayer(client, player);

        if (!AntiHungerData.isWalkReportActive()) {
            // Feature just went off: hand the truth back once, then stop managing it.
            if (tracking) {
                send(client, player, player.isSprinting());
                tracking = false;
            }
            return;
        }

        boolean desired = spoof ? false : player.isSprinting();

        if (!tracking) {
            tracking = true;
            serverThinksSprinting = !desired; // force one send to establish the state
        }

        if (desired != serverThinksSprinting) {
            send(client, player, desired);
        }
    }

    private static void send(Minecraft client, LocalPlayer player, boolean sprinting) {
        if (client.getConnection() == null) return;

        sendingOwn = true;
        try {
            client.getConnection().send(new ServerboundPlayerCommandPacket(player,
                sprinting
                    ? ServerboundPlayerCommandPacket.Action.START_SPRINTING
                    : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        } finally {
            sendingOwn = false;
        }
        serverThinksSprinting = sprinting;
    }

    private static boolean nearAnyPlayer(Minecraft client, LocalPlayer self) {
        if (!AntiHungerData.isPauseNearPlayers() || client.level == null) return false;

        double radius = AntiHungerData.getPauseRadius();
        double radiusSqr = radius * radius;
        for (Player other : client.level.players()) {
            if (other == self || other.isSpectator()) continue;
            if (other.distanceToSqr(self) <= radiusSqr) return true;
        }
        return false;
    }
}
