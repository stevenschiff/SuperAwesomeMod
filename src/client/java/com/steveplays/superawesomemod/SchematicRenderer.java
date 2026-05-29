package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders the schematic ghost overlay or verifier overlay in world space.
 * Follows the same pattern as {@link XrayRenderer}.
 */
public final class SchematicRenderer {

    private SchematicRenderer() {}

    private static final int RENDER_RADIUS = 32;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(SchematicRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!SchematicData.isEnabled()) return;

        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();
        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();

        int renderMode = SchematicData.getRenderMode();

        if (renderMode == 0) {
            renderGhost(consumers, matrix, camPos, placement);
        } else {
            renderVerifier(consumers, matrix, pose, camPos, placement);
        }
    }

    /**
     * Renders translucent colored cubes for each non-air block in the schematic.
     */
    private static void renderGhost(MultiBufferSource consumers, Matrix4f matrix,
                                     Vec3 camPos, SchematicPlacement placement) {
        VertexConsumer buf = consumers.getBuffer(SchematicRenderType.SCHEMATIC_GHOST);
        float alpha = SchematicData.getGhostAlpha();

        BlockPos min = placement.getMin();
        BlockPos max = placement.getMax();

        int camX = (int) Math.floor(camPos.x);
        int camY = (int) Math.floor(camPos.y);
        int camZ = (int) Math.floor(camPos.z);

        int startX = Math.max(min.getX(), camX - RENDER_RADIUS);
        int startY = Math.max(min.getY(), camY - RENDER_RADIUS);
        int startZ = Math.max(min.getZ(), camZ - RENDER_RADIUS);
        int endX = Math.min(max.getX(), camX + RENDER_RADIUS);
        int endY = Math.min(max.getY(), camY + RENDER_RADIUS);
        int endZ = Math.min(max.getZ(), camZ + RENDER_RADIUS);

        // Layer mode: restrict Y range
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
                    BlockState state = placement.getBlockStateAt(mutable);
                    if (state.isAir()) continue;

                    int color = getBlockColor(state);
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >> 8) & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;

                    float x0 = (float) (x - camPos.x);
                    float y0 = (float) (y - camPos.y);
                    float z0 = (float) (z - camPos.z);
                    float x1 = x0 + 1f;
                    float y1 = y0 + 1f;
                    float z1 = z0 + 1f;

                    renderFilledBox(buf, matrix, x0, y0, z0, x1, y1, z1, r, g, b, alpha);
                }
            }
        }
    }

    /**
     * Renders wireframe boxes colored by verification status.
     */
    private static void renderVerifier(MultiBufferSource consumers, Matrix4f matrix,
                                        PoseStack.Pose pose, Vec3 camPos,
                                        SchematicPlacement placement) {
        VertexConsumer buf = consumers.getBuffer(SchematicRenderType.SCHEMATIC_OUTLINE);

        BlockPos min = placement.getMin();
        BlockPos max = placement.getMax();

        int camX = (int) Math.floor(camPos.x);
        int camY = (int) Math.floor(camPos.y);
        int camZ = (int) Math.floor(camPos.z);

        int startX = Math.max(min.getX(), camX - RENDER_RADIUS);
        int startY = Math.max(min.getY(), camY - RENDER_RADIUS);
        int startZ = Math.max(min.getZ(), camZ - RENDER_RADIUS);
        int endX = Math.min(max.getX(), camX + RENDER_RADIUS);
        int endY = Math.min(max.getY(), camY + RENDER_RADIUS);
        int endZ = Math.min(max.getZ(), camZ + RENDER_RADIUS);

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
                    SchematicVerifier.Status status = SchematicVerifier.getStatus(mutable);
                    if (status == null || status == SchematicVerifier.Status.CORRECT) continue;

                    float r, g, b;
                    switch (status) {
                        case WRONG_BLOCK -> { r = 1.0f; g = 0.0f; b = 0.0f; }  // red
                        case MISSING     -> { r = 1.0f; g = 1.0f; b = 0.0f; }  // yellow
                        case EXTRA       -> { r = 1.0f; g = 0.0f; b = 1.0f; }  // magenta
                        default          -> { r = 1.0f; g = 1.0f; b = 1.0f; }
                    }

                    float x0 = (float) (x - camPos.x);
                    float y0 = (float) (y - camPos.y);
                    float z0 = (float) (z - camPos.z);
                    float x1 = x0 + 1f;
                    float y1 = y0 + 1f;
                    float z1 = z0 + 1f;

                    renderBoxLines(buf, matrix, pose, x0, y0, z0, x1, y1, z1, r, g, b, 1.0f);
                }
            }
        }
    }

    /**
     * Returns a color for a block state based on its material category.
     */
    private static int getBlockColor(BlockState state) {
        String name = state.getBlock().getName().getString().toLowerCase();

        // Stone / mineral
        if (name.contains("stone") || name.contains("cobble") || name.contains("brick")
            || name.contains("concrete") || name.contains("deepslate") || name.contains("andesite")
            || name.contains("diorite") || name.contains("granite") || name.contains("tuff")) {
            return 0x888888;
        }
        // Wood / organic
        if (name.contains("log") || name.contains("plank") || name.contains("wood")
            || name.contains("stripped") || name.contains("bamboo")) {
            return 0x8B6914;
        }
        // Glass / transparent
        if (name.contains("glass") || name.contains("ice")) {
            return 0x88CCEE;
        }
        // Redstone
        if (name.contains("redstone") || name.contains("piston") || name.contains("repeater")
            || name.contains("comparator") || name.contains("observer") || name.contains("hopper")) {
            return 0xCC3333;
        }
        // Water / fluid
        if (name.contains("water") || name.contains("kelp") || name.contains("seagrass")) {
            return 0x3366CC;
        }
        // Leaves / vegetation
        if (name.contains("leaves") || name.contains("grass") || name.contains("fern")
            || name.contains("vine") || name.contains("moss")) {
            return 0x44AA44;
        }
        // Sand / gravel
        if (name.contains("sand") || name.contains("gravel")) {
            return 0xDDCC88;
        }
        // Ore
        if (name.contains("ore")) {
            return 0xFFAA33;
        }
        // Wool / terracotta
        if (name.contains("wool") || name.contains("terracotta") || name.contains("carpet")) {
            return 0xCC8866;
        }
        // Default
        return 0xAAAAAA;
    }

    // --- Geometry helpers ---

    private static void renderFilledBox(VertexConsumer buf, Matrix4f mat,
                                         float x0, float y0, float z0,
                                         float x1, float y1, float z1,
                                         float r, float g, float b, float a) {
        // Bottom face (y0)
        buf.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);

        // Top face (y1)
        buf.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);

        // North face (z0)
        buf.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);

        // South face (z1)
        buf.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);

        // West face (x0)
        buf.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);

        // East face (x1)
        buf.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        buf.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
    }

    private static void renderBoxLines(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1,
                                        float r, float g, float b, float a) {
        // Bottom
        edge(buf, mat, pose, x0, y0, z0, x1, y0, z0, r, g, b, a, 1, 0, 0);
        edge(buf, mat, pose, x1, y0, z0, x1, y0, z1, r, g, b, a, 0, 0, 1);
        edge(buf, mat, pose, x1, y0, z1, x0, y0, z1, r, g, b, a, -1, 0, 0);
        edge(buf, mat, pose, x0, y0, z1, x0, y0, z0, r, g, b, a, 0, 0, -1);
        // Top
        edge(buf, mat, pose, x0, y1, z0, x1, y1, z0, r, g, b, a, 1, 0, 0);
        edge(buf, mat, pose, x1, y1, z0, x1, y1, z1, r, g, b, a, 0, 0, 1);
        edge(buf, mat, pose, x1, y1, z1, x0, y1, z1, r, g, b, a, -1, 0, 0);
        edge(buf, mat, pose, x0, y1, z1, x0, y1, z0, r, g, b, a, 0, 0, -1);
        // Verticals
        edge(buf, mat, pose, x0, y0, z0, x0, y1, z0, r, g, b, a, 0, 1, 0);
        edge(buf, mat, pose, x1, y0, z0, x1, y1, z0, r, g, b, a, 0, 1, 0);
        edge(buf, mat, pose, x1, y0, z1, x1, y1, z1, r, g, b, a, 0, 1, 0);
        edge(buf, mat, pose, x0, y0, z1, x0, y1, z1, r, g, b, a, 0, 1, 0);
    }

    private static void edge(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a,
                              float nx, float ny, float nz) {
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
        buf.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
    }
}
