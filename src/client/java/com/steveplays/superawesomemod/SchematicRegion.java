package com.steveplays.superawesomemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One region within a loaded {@code .litematic} schematic.
 * Holds position, size, a palette of {@link BlockState}s, and the packed block data.
 */
public final class SchematicRegion {

    private final String name;
    private final BlockPos position; // relative to schematic origin
    private final Vec3i size;        // may contain negative components (direction)
    private final BlockState[] palette;
    private final LitematicaBitArray blockStates;

    public SchematicRegion(String name, BlockPos position, Vec3i size,
                           BlockState[] palette, LitematicaBitArray blockStates) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.palette = palette;
        this.blockStates = blockStates;
    }

    /**
     * Returns the block state at the given position relative to this region's origin.
     * Coordinates must be in [0, abs(sizeAxis)) for each axis.
     */
    public BlockState getBlockState(int x, int y, int z) {
        int sx = Math.abs(size.getX());
        int sy = Math.abs(size.getY());
        int sz = Math.abs(size.getZ());

        if (x < 0 || x >= sx || y < 0 || y >= sy || z < 0 || z >= sz) {
            return Blocks.AIR.defaultBlockState();
        }

        int index = (y * sx * sz) + (z * sx) + x;
        int paletteIndex = blockStates.get(index);

        if (paletteIndex < 0 || paletteIndex >= palette.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return palette[paletteIndex];
    }

    public String getName() { return name; }
    public BlockPos getPosition() { return position; }
    public Vec3i getSize() { return size; }

    public int getAbsSizeX() { return Math.abs(size.getX()); }
    public int getAbsSizeY() { return Math.abs(size.getY()); }
    public int getAbsSizeZ() { return Math.abs(size.getZ()); }

    /**
     * Returns the minimum corner of this region in region-local coordinates,
     * accounting for negative size components.
     */
    public BlockPos getMinPos() {
        int minX = size.getX() < 0 ? position.getX() + size.getX() + 1 : position.getX();
        int minY = size.getY() < 0 ? position.getY() + size.getY() + 1 : position.getY();
        int minZ = size.getZ() < 0 ? position.getZ() + size.getZ() + 1 : position.getZ();
        return new BlockPos(minX, minY, minZ);
    }

    /**
     * Returns the maximum corner (exclusive) of this region in region-local coordinates.
     */
    public BlockPos getMaxPos() {
        BlockPos min = getMinPos();
        return min.offset(getAbsSizeX(), getAbsSizeY(), getAbsSizeZ());
    }

    public BlockState[] getPalette() { return palette; }
}
