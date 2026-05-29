package com.steveplays.superawesomemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory representation of a loaded {@code .litematic} schematic.
 */
public final class Schematic {

    private final String name;
    private final String author;
    private final Vec3i enclosingSize;
    private final List<SchematicRegion> regions;
    private final int totalBlocks;

    public Schematic(String name, String author, Vec3i enclosingSize,
                     List<SchematicRegion> regions, int totalBlocks) {
        this.name = name;
        this.author = author;
        this.enclosingSize = enclosingSize;
        this.regions = List.copyOf(regions);
        this.totalBlocks = totalBlocks;
    }

    /**
     * Returns the block state at the given position relative to the schematic origin (0,0,0).
     * Iterates regions to find which one (if any) contains the position.
     */
    public BlockState getBlockState(int x, int y, int z) {
        for (SchematicRegion region : regions) {
            BlockPos min = region.getMinPos();
            BlockPos max = region.getMaxPos();

            if (x >= min.getX() && x < max.getX()
                && y >= min.getY() && y < max.getY()
                && z >= min.getZ() && z < max.getZ()) {
                int rx = x - min.getX();
                int ry = y - min.getY();
                int rz = z - min.getZ();
                return region.getBlockState(rx, ry, rz);
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Counts all non-air blocks grouped by block state.
     */
    public Map<BlockState, Integer> countBlocks() {
        Map<BlockState, Integer> counts = new HashMap<>();
        for (SchematicRegion region : regions) {
            int sx = region.getAbsSizeX();
            int sy = region.getAbsSizeY();
            int sz = region.getAbsSizeZ();
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    for (int x = 0; x < sx; x++) {
                        BlockState state = region.getBlockState(x, y, z);
                        if (!state.isAir()) {
                            counts.merge(state, 1, Integer::sum);
                        }
                    }
                }
            }
        }
        return counts;
    }

    public String getName() { return name; }
    public String getAuthor() { return author; }
    public Vec3i getEnclosingSize() { return enclosingSize; }
    public List<SchematicRegion> getRegions() { return regions; }
    public int getTotalBlocks() { return totalBlocks; }

    /**
     * Returns the minimum corner across all regions.
     */
    public BlockPos getMinPos() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (SchematicRegion r : regions) {
            BlockPos min = r.getMinPos();
            minX = Math.min(minX, min.getX());
            minY = Math.min(minY, min.getY());
            minZ = Math.min(minZ, min.getZ());
        }
        return new BlockPos(minX, minY, minZ);
    }

    /**
     * Returns the maximum corner (exclusive) across all regions.
     */
    public BlockPos getMaxPos() {
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (SchematicRegion r : regions) {
            BlockPos max = r.getMaxPos();
            maxX = Math.max(maxX, max.getX());
            maxY = Math.max(maxY, max.getY());
            maxZ = Math.max(maxZ, max.getZ());
        }
        return new BlockPos(maxX, maxY, maxZ);
    }
}
