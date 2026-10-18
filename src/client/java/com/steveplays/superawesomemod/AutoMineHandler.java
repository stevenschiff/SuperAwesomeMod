package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Walks to the nearest block of the marked type and mines it.
 *
 * <p>Runs as a small state machine rather than doing everything each tick: find a target,
 * path to it, walk the path, aim, mine, repeat. Each stage can fail into a stopped state
 * with a reason, because a bot that quietly stands still is worse than one that says it
 * gave up.
 *
 * <p>Movement is published through {@link #getMoveTarget()} and applied by
 * {@code AutoMineInputMixin} as ordinary key presses, so the server sees a walking player.
 * Nothing here teleports.
 */
public final class AutoMineHandler {

    /** How close to a path node before moving to the next one. */
    private static final double NODE_REACHED = 0.6;
    /** Interaction range to leave when standing next to the block being mined. */
    private static final double MINE_REACH = 3.5;
    /** How far down the path to look while walking, in blocks. */
    private static final double LOOK_AHEAD = 3.5;
    private static final int RESCAN_INTERVAL = 20;

    private enum State { SEARCH, PATH, WALK, MINE }

    private static State state = State.SEARCH;
    private static BlockPos targetBlock = null;
    private static List<BlockPos> path = List.of();
    private static int pathIndex = 0;
    private static int rescanTimer = 0;
    private static int actionDelay = 0;
    private static float lastHealth = -1f;
    private static Vec3 aimPoint = null;

    /** Where the walker currently wants to go, or null when it should stand still. */
    private static BlockPos moveTarget = null;
    private static boolean wantJump = false;

    private AutoMineHandler() {}

    public static BlockPos getMoveTarget() { return moveTarget; }
    public static boolean  wantsJump()     { return wantJump; }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            reset();
            return;
        }

        if (!AutoMineData.isRunning() || client.screen != null) {
            moveTarget = null;
            wantJump = false;
            // Give the mouse back the moment the run ends, so nothing keeps steering.
            RotationHelper.release();
            lastHealth = player.getHealth();
            return;
        }

        // Safety stop: anything that hurts ends the run. This is also what covers lava,
        // after the fact rather than before — the path avoids fluids, but a mob or a fall
        // can still land a hit, and continuing to mine through it is how a bot dies.
        if (lastHealth >= 0f && player.getHealth() < lastHealth - 0.01f) {
            AutoMineData.stop("Stopped: took damage");
            reset();
            return;
        }
        lastHealth = player.getHealth();

        if (actionDelay > 0) {
            actionDelay--;
            return;
        }

        switch (state) {
            case SEARCH -> search(client, player);
            case PATH   -> buildPath(client, player);
            case WALK   -> walk(client, player);
            case MINE   -> mine(client, player);
        }
    }

    private static void search(Minecraft client, LocalPlayer player) {
        moveTarget = null;
        Block want = AutoMineData.getTarget();
        if (want == null) {
            AutoMineData.stop("Stopped: nothing marked");
            return;
        }

        if (rescanTimer-- > 0) return;
        rescanTimer = RESCAN_INTERVAL;

        BlockPos found = nearest(client.level, player.blockPosition(), want,
                                 AutoMineData.getSearchRadius());
        if (found == null) {
            AutoMineData.stop("Stopped: no " + AutoMineData.getTargetName() + " nearby");
            return;
        }

        targetBlock = found;
        AutoMineData.setStatus("Pathing");
        state = State.PATH;
    }

    private static void buildPath(Minecraft client, LocalPlayer player) {
        moveTarget = null;
        if (targetBlock == null) {
            state = State.SEARCH;
            return;
        }

        // Already close enough to just mine it.
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetBlock))
                <= MINE_REACH * MINE_REACH) {
            beginMining();
            return;
        }

        path = PathFinder.findPath(client.level, player.blockPosition(), targetBlock, MINE_REACH);
        if (path.isEmpty()) {
            // Walk-only by design: a sealed target has no route and that is not a bug.
            AutoMineData.stop("Stopped: no walkable route");
            return;
        }
        pathIndex = 0;
        AutoMineData.setStatus("Walking");
        state = State.WALK;
    }

    private static void walk(Minecraft client, LocalPlayer player) {
        if (targetBlock == null || client.level.getBlockState(targetBlock).getBlock()
                != AutoMineData.getTarget()) {
            // Someone else mined it, or it changed under us.
            state = State.SEARCH;
            return;
        }

        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetBlock))
                <= MINE_REACH * MINE_REACH) {
            beginMining();
            return;
        }

        if (pathIndex >= path.size()) {
            state = State.PATH;
            return;
        }

        BlockPos node = path.get(pathIndex);
        Vec3 nodeCentre = new Vec3(node.getX() + 0.5, node.getY(), node.getZ() + 0.5);
        double flatDistSqr = (player.getX() - nodeCentre.x) * (player.getX() - nodeCentre.x)
                           + (player.getZ() - nodeCentre.z) * (player.getZ() - nodeCentre.z);

        if (flatDistSqr < NODE_REACHED * NODE_REACHED) {
            pathIndex++;
            return;
        }

        moveTarget = node;
        wantJump = node.getY() > player.blockPosition().getY() && player.onGround();

        // Look where we are going, not at the target — staring at the ore while
        // side-stepping toward it is not how a person walks. Aim well down the path
        // rather than at the node underfoot: the yaw to a point half a block away
        // swings hard for a small sideways step, which is what made the camera judder.
        RotationHelper.lookAt(player, lookAheadPoint(player), 8f);
    }

    /**
     * A point several blocks along the remaining path, at head height, so the direction
     * to it changes slowly as the player walks. Falls back to the furthest node left when
     * the path is shorter than {@link #LOOK_AHEAD}.
     */
    private static Vec3 lookAheadPoint(LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        double eyeOffset = eye.y - player.getY();
        Vec3 chosen = null;

        for (int i = pathIndex; i < path.size(); i++) {
            BlockPos p = path.get(i);
            // Head height above the node, so pitch tracks the slope of the route
            // instead of pointing down at the player's own feet.
            chosen = new Vec3(p.getX() + 0.5, p.getY() + eyeOffset, p.getZ() + 0.5);
            double dx = chosen.x - eye.x;
            double dz = chosen.z - eye.z;
            if (dx * dx + dz * dz >= LOOK_AHEAD * LOOK_AHEAD) break;
        }

        if (chosen != null) return chosen;
        return targetBlock != null
            ? Vec3.atCenterOf(targetBlock)
            : eye.add(player.getLookAngle());
    }

    private static void beginMining() {
        moveTarget = null;
        wantJump = false;
        aimPoint = RotationHelper.humanAimPoint(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        // A short pause before swinging, so arriving and mining aren't the same instant.
        actionDelay = RotationHelper.jitterTicks(2, 4);
        AutoMineData.setStatus("Mining");
        state = State.MINE;
    }

    private static void mine(Minecraft client, LocalPlayer player) {
        moveTarget = null;

        if (targetBlock == null) {
            state = State.SEARCH;
            return;
        }
        if (client.level.getBlockState(targetBlock).getBlock() != AutoMineData.getTarget()) {
            // Broken — go again, after a beat.
            actionDelay = RotationHelper.jitterTicks(3, 5);
            state = State.SEARCH;
            return;
        }
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetBlock))
                > MINE_REACH * MINE_REACH) {
            state = State.PATH;
            return;
        }

        if (!RotationHelper.lookAt(player, aimPoint, 4f)) return;

        Direction face = Direction.getApproximateNearest(
            player.getEyePosition().subtract(Vec3.atCenterOf(targetBlock)));
        client.gameMode.continueDestroyBlock(targetBlock, face);
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    /** Nearest matching block by straight-line distance, searched outward from the player. */
    private static BlockPos nearest(Level level, BlockPos from, Block want, int radius) {
        BlockPos best = null;
        double bestSqr = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        int minY = Math.max(level.getMinY(), from.getY() - radius);
        int maxY = Math.min(level.getMaxY(), from.getY() + radius);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(from.getX() + dx, y, from.getZ() + dz);
                    if (level.getBlockState(cursor).getBlock() != want) continue;
                    double d = cursor.distSqr(from);
                    if (d < bestSqr) {
                        bestSqr = d;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static void reset() {
        state = State.SEARCH;
        targetBlock = null;
        path = List.of();
        pathIndex = 0;
        moveTarget = null;
        wantJump = false;
        aimPoint = null;
        actionDelay = 0;
        RotationHelper.release();
    }
}
