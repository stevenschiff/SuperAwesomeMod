package com.steveplays.superawesomemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A loaded schematic placed at a specific world position with optional rotation and mirror.
 */
public final class SchematicPlacement {

    private final Schematic schematic;
    private BlockPos origin;
    private Rotation rotation;
    private boolean mirror;

    public SchematicPlacement(Schematic schematic, BlockPos origin) {
        this.schematic = schematic;
        this.origin = origin;
        this.rotation = Rotation.NONE;
        this.mirror = false;
    }

    /**
     * Returns the expected block state at the given world position,
     * applying rotation and mirror transforms.
     */
    public BlockState getBlockStateAt(BlockPos worldPos) {
        int rx = worldPos.getX() - origin.getX();
        int ry = worldPos.getY() - origin.getY();
        int rz = worldPos.getZ() - origin.getZ();

        // Reverse rotation to map world coords back to schematic coords
        int[] local = reverseTransform(rx, ry, rz);
        BlockState state = schematic.getBlockState(local[0], local[1], local[2]);

        if (state.isAir()) return state;

        // Apply transforms to the block state
        if (mirror) {
            state = state.mirror(Mirror.FRONT_BACK);
        }
        state = state.rotate(rotation);
        return state;
    }

    /**
     * Whether the given world position falls within this placement's bounds.
     */
    public boolean containsPos(BlockPos worldPos) {
        int rx = worldPos.getX() - origin.getX();
        int ry = worldPos.getY() - origin.getY();
        int rz = worldPos.getZ() - origin.getZ();

        int[] local = reverseTransform(rx, ry, rz);
        BlockPos min = schematic.getMinPos();
        BlockPos max = schematic.getMaxPos();

        return local[0] >= min.getX() && local[0] < max.getX()
            && local[1] >= min.getY() && local[1] < max.getY()
            && local[2] >= min.getZ() && local[2] < max.getZ();
    }

    /**
     * Reverses rotation and mirror to convert world-relative coords to schematic coords.
     */
    private int[] reverseTransform(int x, int y, int z) {
        if (mirror) {
            x = -x;
        }
        // Reverse the rotation
        return switch (rotation) {
            case NONE -> new int[]{x, y, z};
            case CLOCKWISE_90 -> new int[]{z, y, -x};
            case CLOCKWISE_180 -> new int[]{-x, y, -z};
            case COUNTERCLOCKWISE_90 -> new int[]{-z, y, x};
        };
    }

    public BlockPos getMin() {
        BlockPos sMin = schematic.getMinPos();
        BlockPos sMax = schematic.getMaxPos();
        // Transform all 4 horizontal corners and find the actual min
        int[][] corners = {
            forwardTransform(sMin.getX(), sMin.getY(), sMin.getZ()),
            forwardTransform(sMax.getX() - 1, sMin.getY(), sMin.getZ()),
            forwardTransform(sMin.getX(), sMin.getY(), sMax.getZ() - 1),
            forwardTransform(sMax.getX() - 1, sMin.getY(), sMax.getZ() - 1)
        };
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (int[] c : corners) {
            minX = Math.min(minX, c[0]);
            minZ = Math.min(minZ, c[2]);
        }
        return origin.offset(minX, sMin.getY(), minZ);
    }

    public BlockPos getMax() {
        BlockPos sMin = schematic.getMinPos();
        BlockPos sMax = schematic.getMaxPos();
        int[][] corners = {
            forwardTransform(sMin.getX(), sMax.getY(), sMin.getZ()),
            forwardTransform(sMax.getX() - 1, sMax.getY(), sMin.getZ()),
            forwardTransform(sMin.getX(), sMax.getY(), sMax.getZ() - 1),
            forwardTransform(sMax.getX() - 1, sMax.getY(), sMax.getZ() - 1)
        };
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int maxY = corners[0][1];
        for (int[] c : corners) {
            maxX = Math.max(maxX, c[0]);
            maxZ = Math.max(maxZ, c[2]);
        }
        // +1 because getMax is exclusive
        return origin.offset(maxX + 1, maxY, maxZ + 1);
    }

    private int[] forwardTransform(int x, int y, int z) {
        int rx, rz;
        switch (rotation) {
            case CLOCKWISE_90 -> { rx = -z; rz = x; }
            case CLOCKWISE_180 -> { rx = -x; rz = -z; }
            case COUNTERCLOCKWISE_90 -> { rx = z; rz = -x; }
            default -> { rx = x; rz = z; }
        }
        if (mirror) {
            rx = -rx;
        }
        return new int[]{rx, y, rz};
    }

    // Getters and setters
    public Schematic getSchematic() { return schematic; }
    public BlockPos getOrigin() { return origin; }
    public void setOrigin(BlockPos origin) { this.origin = origin; }

    public Rotation getRotation() { return rotation; }
    public void setRotation(Rotation rotation) { this.rotation = rotation; }
    public void cycleRotation() {
        this.rotation = switch (rotation) {
            case NONE -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_180;
            case CLOCKWISE_180 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.NONE;
        };
    }

    public boolean isMirror() { return mirror; }
    public void setMirror(boolean mirror) { this.mirror = mirror; }
    public void toggleMirror() { this.mirror = !this.mirror; }

    public String getRotationName() {
        return switch (rotation) {
            case NONE -> "0°";
            case CLOCKWISE_90 -> "90°";
            case CLOCKWISE_180 -> "180°";
            case COUNTERCLOCKWISE_90 -> "270°";
        };
    }
}
