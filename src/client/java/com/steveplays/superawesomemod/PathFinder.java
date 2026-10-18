package com.steveplays.superawesomemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A bounded A* over standable positions, used to walk to a block worth mining.
 *
 * <p>Deliberately walk-only: it never counts a solid block as passable, so it will not
 * dig a tunnel to reach anything. That was the explicit choice — the path has to reach a
 * standing spot near the target, and only the target itself gets broken. Exposed ore in a
 * cave is reachable; ore sealed inside stone is not, and that reads as "no route".
 *
 * <p>Bounded in two directions: a search box around the player, and a ceiling on nodes
 * expanded. Minecraft worlds are large enough that an unbounded search would happily
 * stall the client for seconds looking for a route that does not exist.
 */
public final class PathFinder {

    /** How far from the player, in blocks, the search box extends. */
    private static final int RANGE = 48;
    /** Ceiling on expanded nodes, so a hopeless search fails fast instead of hanging. */
    private static final int MAX_NODES = 6000;
    /** Longest drop the path will take voluntarily. */
    private static final int MAX_DROP = 3;

    private PathFinder() {}

    /** Can a player stand with their feet at this position? */
    public static boolean standable(BlockGetter level, BlockPos pos) {
        return passable(level, pos)
            && passable(level, pos.above())
            && solidTop(level, pos.below());
    }

    private static boolean passable(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getCollisionShape(level, pos).isEmpty()) return false;
        // Fluids are walkable in principle but swimming and lava are both ways to end a
        // run badly, so the path avoids them entirely.
        FluidState fluid = state.getFluidState();
        return fluid.isEmpty();
    }

    private static boolean solidTop(BlockGetter level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /**
     * Finds a walking route from {@code start} to any standable position within
     * {@code reach} of {@code target}. Returns an empty list when there is no route.
     */
    public static List<BlockPos> findPath(BlockGetter level, BlockPos start, BlockPos target,
                                          double reach) {
        if (!standable(level, start)) {
            // Feet may be a block off after a jump or on a slab; try directly below.
            BlockPos below = start.below();
            if (standable(level, below)) start = below;
            else return List.of();
        }

        double reachSqr = reach * reach;
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Integer> gScore = new HashMap<>();
        Set<BlockPos> closed = new HashSet<>();
        PriorityQueue<BlockPos> open =
            new PriorityQueue<>(Comparator.comparingDouble(p -> gScore.getOrDefault(p, Integer.MAX_VALUE)
                                                              + heuristic(p, target)));

        gScore.put(start, 0);
        open.add(start);
        int expanded = 0;

        while (!open.isEmpty() && expanded++ < MAX_NODES) {
            BlockPos current = open.poll();
            if (!closed.add(current)) continue;

            if (current.distSqr(target) <= reachSqr) {
                return reconstruct(cameFrom, current);
            }

            for (BlockPos next : neighbours(level, current)) {
                if (closed.contains(next)) continue;
                if (Math.abs(next.getX() - start.getX()) > RANGE
                    || Math.abs(next.getZ() - start.getZ()) > RANGE
                    || Math.abs(next.getY() - start.getY()) > RANGE) continue;

                int tentative = gScore.getOrDefault(current, Integer.MAX_VALUE) + stepCost(current, next);
                if (tentative < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
                    cameFrom.put(next, current);
                    gScore.put(next, tentative);
                    open.add(next);
                }
            }
        }
        return List.of();
    }

    private static int stepCost(BlockPos from, BlockPos to) {
        // Climbing is slower than walking, and falling is slightly discouraged so the
        // path prefers a gentle route to a cliff when both exist.
        if (to.getY() > from.getY()) return 14;
        if (to.getY() < from.getY()) return 12;
        return 10;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distSqr(b)) * 10.0;
    }

    private static List<BlockPos> neighbours(BlockGetter level, BlockPos pos) {
        List<BlockPos> out = new ArrayList<>(8);
        BlockPos[] flat = {
            pos.north(), pos.south(), pos.east(), pos.west()
        };

        for (BlockPos side : flat) {
            if (standable(level, side)) {
                out.add(side);
                continue;
            }
            // Step up one block.
            BlockPos up = side.above();
            if (standable(level, up) && passable(level, pos.above(2))) {
                out.add(up);
                continue;
            }
            // Drop down, but only a survivable distance.
            for (int d = 1; d <= MAX_DROP; d++) {
                BlockPos down = side.below(d);
                if (standable(level, down)) {
                    out.add(down);
                    break;
                }
                if (!passable(level, down)) break;
            }
        }
        return out;
    }

    private static List<BlockPos> reconstruct(Map<BlockPos, BlockPos> cameFrom, BlockPos end) {
        ArrayDeque<BlockPos> path = new ArrayDeque<>();
        BlockPos cur = end;
        while (cur != null) {
            path.addFirst(cur);
            cur = cameFrom.get(cur);
        }
        List<BlockPos> list = new ArrayList<>(path);
        if (!list.isEmpty()) list.remove(0); // we are already standing on the first node
        return Collections.unmodifiableList(list);
    }
}
