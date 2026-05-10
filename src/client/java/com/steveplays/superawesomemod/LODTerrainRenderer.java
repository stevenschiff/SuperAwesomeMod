package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Map;

/**
 * Renders LOD terrain as colored wireframe grid in world space.
 * Each height sample becomes a flat square outline at the correct elevation.
 */
public final class LODTerrainRenderer {

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

        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();

        int viewDist = 12; // skip chunks within server view distance
        int pcx = LODChunkData.getPlayerChunkX();
        int pcz = LODChunkData.getPlayerChunkZ();
        int lodRadius = LODChunkData.getLodRadius();

        VertexConsumer buf = consumers.getBuffer(RenderTypes.lines());

        // Collect entries to avoid concurrent modification.
        var entries = new ArrayList<Map.Entry<Long, int[]>>();
        LODChunkData.forEach((key, heights) -> entries.add(Map.entry(key, heights)));

        for (var entry : entries) {
            int cx = LODChunkData.unpackX(entry.getKey());
            int cz = LODChunkData.unpackZ(entry.getKey());

            int dx = cx - pcx;
            int dz = cz - pcz;
            // Skip chunks within server's normal render distance.
            if (Math.abs(dx) <= viewDist && Math.abs(dz) <= viewDist) continue;

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

                    // Draw 4 edges of a flat square at the sample height.
                    edge(buf, matrix, pose, x0, y, z0, x1, y, z0, r, g, b, alpha, 1, 0, 0);
                    edge(buf, matrix, pose, x1, y, z0, x1, y, z1, r, g, b, alpha, 0, 0, 1);
                    edge(buf, matrix, pose, x1, y, z1, x0, y, z1, r, g, b, alpha, -1, 0, 0);
                    edge(buf, matrix, pose, x0, y, z1, x0, y, z0, r, g, b, alpha, 0, 0, -1);
                }
            }
        }
    }

    private static void edge(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a,
                             float nx, float ny, float nz) {
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        buf.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }

    private static int getColorForHeight(int height) {
        if (height < 63)  return 0x4488AA; // water blue
        if (height < 80)  return 0x55AA55; // plains green
        if (height < 120) return 0x667744; // hills
        if (height < 180) return 0x888888; // mountains gray
        return 0xDDDDDD; // peaks white
    }
}
