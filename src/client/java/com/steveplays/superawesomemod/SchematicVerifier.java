package com.steveplays.superawesomemod;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Compares placed blocks in the world against the expected schematic blocks.
 * Caches results and rescans periodically (tick-throttled).
 */
public final class SchematicVerifier {

    private SchematicVerifier() {}

    public enum Status {
        CORRECT,
        WRONG_BLOCK,
        MISSING,
        EXTRA
    }

    private static final Map<BlockPos, Status> cache = new HashMap<>();
    private static int tickCounter = 0;
    private static final int SCAN_INTERVAL = 10;
    private static final int SCAN_RADIUS = 32;

    private static int correctCount = 0;
    private static int wrongCount = 0;
    private static int missingCount = 0;
    private static int extraCount = 0;

    /**
     * Returns the cached verification status at the given position, or null if not scanned.
     */
    public static Status getStatus(BlockPos pos) {
        return cache.get(pos);
    }

    public static int getCorrectCount() { return correctCount; }
    public static int getWrongCount() { return wrongCount; }
    public static int getMissingCount() { return missingCount; }
    public static int getExtraCount() { return extraCount; }

    /**
     * Called every client tick when verifier mode is active.
     */
    public static void tick(ClientLevel level, SchematicPlacement placement) {
        if (level == null || placement == null) return;

        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) return;
        tickCounter = 0;

        rescan(level, placement);
    }

    public static void clearCache() {
        cache.clear();
        correctCount = 0;
        wrongCount = 0;
        missingCount = 0;
        extraCount = 0;
    }

    private static void rescan(ClientLevel level, SchematicPlacement placement) {
        cache.clear();
        correctCount = 0;
        wrongCount = 0;
        missingCount = 0;
        extraCount = 0;

        BlockPos min = placement.getMin();
        BlockPos max = placement.getMax();

        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return;

        BlockPos playerPos = player.blockPosition();

        int startX = Math.max(min.getX(), playerPos.getX() - SCAN_RADIUS);
        int startY = Math.max(min.getY(), playerPos.getY() - SCAN_RADIUS);
        int startZ = Math.max(min.getZ(), playerPos.getZ() - SCAN_RADIUS);
        int endX = Math.min(max.getX(), playerPos.getX() + SCAN_RADIUS);
        int endY = Math.min(max.getY(), playerPos.getY() + SCAN_RADIUS);
        int endZ = Math.min(max.getZ(), playerPos.getZ() + SCAN_RADIUS);

        // Layer mode restriction
        if (SchematicData.isLayerMode()) {
            int layerY = min.getY() + SchematicData.getCurrentLayer();
            startY = layerY;
            endY = layerY + 1;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = startY; y < endY; y++) {
            for (int z = startZ; z < endZ; z++) {
                for (int x = startX; x < endX; x++) {
                    mutable.set(x, y, z);

                    BlockState expected = placement.getBlockStateAt(mutable);
                    BlockState actual = level.getBlockState(mutable);

                    boolean expectedAir = expected.isAir();
                    boolean actualAir = actual.isAir();

                    Status status;
                    if (expectedAir && actualAir) {
                        continue; // both air, skip
                    } else if (expectedAir && !actualAir) {
                        status = Status.EXTRA;
                        extraCount++;
                    } else if (!expectedAir && actualAir) {
                        status = Status.MISSING;
                        missingCount++;
                    } else if (expected.equals(actual)) {
                        status = Status.CORRECT;
                        correctCount++;
                    } else {
                        status = Status.WRONG_BLOCK;
                        wrongCount++;
                    }

                    cache.put(mutable.immutable(), status);
                }
            }
        }
    }
}
