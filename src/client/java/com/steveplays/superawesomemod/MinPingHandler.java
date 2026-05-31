package com.steveplays.superawesomemod;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;

import java.util.concurrent.TimeUnit;

/**
 * Netty handler that delays outbound packets when the player's current ping
 * is below the configured minimum, effectively setting a ping floor.
 */
public class MinPingHandler extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
        if (!MinPingData.isEnabled() || MinPingData.getMinPingMs() <= 0) {
            super.write(ctx, msg, promise);
            return;
        }

        int currentPing = getCurrentPing();
        int minPing = MinPingData.getMinPingMs();

        if (currentPing >= minPing || currentPing <= 0) {
            // Already above floor or unknown — pass through immediately.
            super.write(ctx, msg, promise);
            return;
        }

        // Delay the packet by half the difference (outbound only = half RTT).
        long delayMs = (long) (minPing - currentPing) / 2;
        if (delayMs <= 0) {
            super.write(ctx, msg, promise);
            return;
        }

        ctx.executor().schedule(() -> {
            ctx.write(msg, promise);
            ctx.flush();
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private static int getCurrentPing() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return -1;
        PlayerInfo info = player.connection.getPlayerInfo(player.getUUID());
        return info != null ? info.getLatency() : -1;
    }
}
