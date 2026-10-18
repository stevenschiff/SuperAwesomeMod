package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Breaks everything around you, as fast as the server will actually allow.
 *
 * <p>The rule comes straight from {@code ServerPlayerGameMode.handleBlockBreakAction},
 * which vanilla labels itself:
 *
 * <pre>
 * f = blockState.getDestroyProgress(player, level, pos);
 * if (!blockState.isAir() &amp;&amp; f >= 1.0F) {
 *     this.destroyAndAck(pos, j, "insta mine");
 * } else {
 *     this.isDestroyingBlock = true;   // one block, timed by the server
 * </pre>
 *
 * <p>So a block your tool clears within a single tick dies the moment the packet
 * lands, and there is no cap on how many of those we send per tick — that is the real
 * nuker, and it covers grass, crops, torches, leaves with shears, and everything in
 * creative. Anything harder falls into the second branch, where the server tracks
 * exactly one position, so stone and ore can only ever be worked one at a time no
 * matter the radius. Running the same {@code getDestroyProgress} test here sorts the
 * two groups before anything is sent, rather than spraying packets the server drops.
 */
public final class NukerHandler {

    private NukerHandler() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (!NukerData.isEnabled() || player == null || level == null) return;
        if (client.screen != null || client.gameMode == null) return;

        int radius = NukerData.getRadius();
        double reach = player.blockInteractionRange();
        double reachSqr = reach * reach;
        BlockPos origin = player.blockPosition();

        BlockPos slowest = null;
        float slowestProgress = 0.0f;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);

                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    // The server range-checks every break, so anything past our reach
                    // would just be rejected.
                    if (pos.distToCenterSqr(player.getEyePosition()) > reachSqr) continue;

                    float progress = state.getDestroyProgress(player, level, pos);
                    if (progress <= 0.0f) continue; // unbreakable by hand or by rule

                    if (progress >= 1.0f) {
                        // The server will insta-mine this one; fire and forget.
                        client.gameMode.startDestroyBlock(pos, Direction.UP);
                        client.gameMode.stopDestroyBlock();
                    } else if (!NukerData.isInstantOnly() && progress > slowestProgress) {
                        // Remember the quickest of the slow blocks — only one can be
                        // in progress server-side, so picking the fastest finishes most.
                        slowestProgress = progress;
                        slowest = pos;
                    }
                }
            }
        }

        if (slowest != null) {
            // Continue rather than restart, so progress accumulates instead of resetting.
            client.gameMode.continueDestroyBlock(slowest, Direction.UP);
        }
    }
}
