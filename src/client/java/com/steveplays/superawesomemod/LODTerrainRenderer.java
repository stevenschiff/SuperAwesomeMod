package com.steveplays.superawesomemod;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Map;

/**
 * Renders LOD terrain as solid colored quads in world space.
 * Each height sample becomes a flat filled square at the correct elevation.
 */
public final class LODTerrainRenderer {

    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("superawesomemod", "lod_terrain"))
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final RenderType LOD_TERRAIN = RenderType.create(
            "lod_terrain",
            RenderSetup.builder(PIPELINE).createRenderSetup()
    );

    private LODTerrainRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(LODTerrainRenderer::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext ctx) {
        if (!RenderDistanceData.isEnabled()) return;
        if (LODChunkData.size() == 0) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();

        Matrix4f matrix = ctx.matrices().last().pose();

        int skipDist = LODChunkData.getSkipRadius();
        int pcx = LODChunkData.getPlayerChunkX();
        int pcz = LODChunkData.getPlayerChunkZ();
        int lodRadius = LODChunkData.getLodRadius();

        VertexConsumer buf = consumers.getBuffer(LOD_TERRAIN);

        // Collect entries to avoid concurrent modification.
        var entries = new ArrayList<Map.Entry<Long, int[]>>();
        LODChunkData.forEach((key, heights) -> entries.add(Map.entry(key, heights)));

        for (var entry : entries) {
            int cx = LODChunkData.unpackX(entry.getKey());
            int cz = LODChunkData.unpackZ(entry.getKey());

            int dx = cx - pcx;
            int dz = cz - pcz;
            // Skip chunks within real chunk range.
            if (Math.abs(dx) <= skipDist && Math.abs(dz) <= skipDist) continue;

            int[] heights = entry.getValue();
            int baseBlockX = cx * 16;
            int baseBlockZ = cz * 16;

            float chunkDist = (float) Math.sqrt(dx * dx + dz * dz);
            float alpha = Math.max(0.3f, 1.0f - (chunkDist / lodRadius) * 0.7f);

            for (int sx = 0; sx < LODChunkData.SAMPLES_PER_AXIS; sx++) {
                for (int sz = 0; sz < LODChunkData.SAMPLES_PER_AXIS; sz++) {
                    int height = heights[sx * LODChunkData.SAMPLES_PER_AXIS + sz];

                    float x0 = (float) (baseBlockX + sx * LODChunkData.SAMPLE_SPACING - camPos.x);
                    float z0 = (float) (baseBlockZ + sz * LODChunkData.SAMPLE_SPACING - camPos.z);
                    float y  = (float) (height - camPos.y);
                    float x1 = x0 + LODChunkData.SAMPLE_SPACING;
                    float z1 = z0 + LODChunkData.SAMPLE_SPACING;

                    int color = getColorForHeight(height);
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >> 8) & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;

                    // Filled quad (counter-clockwise winding for upward face).
                    buf.addVertex(matrix, x0, y, z0).setColor(r, g, b, alpha);
                    buf.addVertex(matrix, x0, y, z1).setColor(r, g, b, alpha);
                    buf.addVertex(matrix, x1, y, z1).setColor(r, g, b, alpha);
                    buf.addVertex(matrix, x1, y, z0).setColor(r, g, b, alpha);
                }
            }
        }
    }

    private static int getColorForHeight(int height) {
        if (height < 63)  return 0x4488AA; // water blue
        if (height < 80)  return 0x55AA55; // plains green
        if (height < 120) return 0x667744; // hills
        if (height < 180) return 0x888888; // mountains gray
        return 0xDDDDDD; // peaks white
    }
}
