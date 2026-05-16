package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class XrayRenderer {

    private XrayRenderer() {}

    private static final int SCAN_RADIUS = 64;
    private static final int SCAN_INTERVAL = 10;

    private static final List<OreHit> cachedOres = new ArrayList<>();
    private static int tickCounter = 0;
    private static BlockPos lastScanCenter = BlockPos.ZERO;

    private record OreHit(BlockPos pos, XrayOreEntry entry) {}

    private static Map<Block, XrayOreEntry> blockToEntry;

    private static Map<Block, XrayOreEntry> getBlockLookup() {
        if (blockToEntry == null) {
            blockToEntry = new HashMap<>();
            for (XrayOreEntry entry : XrayOreEntry.ALL) {
                for (Block block : entry.blocks()) {
                    blockToEntry.put(block, entry);
                }
            }
        }
        return blockToEntry;
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XrayRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!XrayData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        // Tick-based re-scanning
        tickCounter++;
        BlockPos playerPos = player.blockPosition();
        boolean needsRescan = tickCounter >= SCAN_INTERVAL
            || lastScanCenter.distManhattan(playerPos) > 16;

        if (needsRescan) {
            tickCounter = 0;
            lastScanCenter = playerPos;
            rescan(level, playerPos);
        }

        if (cachedOres.isEmpty()) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();
        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();

        VertexConsumer buf = consumers.getBuffer(XrayLineRenderType.LINES_XRAY);

        for (OreHit hit : cachedOres) {
            XrayOreEntry ore = hit.entry();
            if (!XrayData.isOreEnabled(ore.name())) continue;

            BlockPos pos = hit.pos();
            float x0 = (float) (pos.getX()     - camPos.x);
            float y0 = (float) (pos.getY()     - camPos.y);
            float z0 = (float) (pos.getZ()     - camPos.z);
            float x1 = (float) (pos.getX() + 1 - camPos.x);
            float y1 = (float) (pos.getY() + 1 - camPos.y);
            float z1 = (float) (pos.getZ() + 1 - camPos.z);

            renderBoxLines(buf, matrix, pose, x0, y0, z0, x1, y1, z1,
                ore.r(), ore.g(), ore.b(), 1.0f);
        }
    }

    private static void rescan(ClientLevel level, BlockPos center) {
        cachedOres.clear();
        Map<Block, XrayOreEntry> lookup = getBlockLookup();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        int minY = Math.max(level.getMinY(), cy - SCAN_RADIUS);
        int maxY = Math.min(level.getMaxY(), cy + SCAN_RADIUS);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = cx - SCAN_RADIUS; x <= cx + SCAN_RADIUS; x++) {
            for (int z = cz - SCAN_RADIUS; z <= cz + SCAN_RADIUS; z++) {
                for (int y = minY; y <= maxY; y++) {
                    mutable.set(x, y, z);
                    BlockState state = level.getBlockState(mutable);
                    XrayOreEntry entry = lookup.get(state.getBlock());
                    if (entry != null) {
                        cachedOres.add(new OreHit(mutable.immutable(), entry));
                    }
                }
            }
        }
    }

    private static void renderBoxLines(VertexConsumer buf, Matrix4f mat,
                                        PoseStack.Pose pose,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1,
                                        float r, float g, float b, float a) {
        // Bottom rectangle
        edge(buf, mat, pose, x0, y0, z0, x1, y0, z0, r, g, b, a, 1, 0, 0);
        edge(buf, mat, pose, x1, y0, z0, x1, y0, z1, r, g, b, a, 0, 0, 1);
        edge(buf, mat, pose, x1, y0, z1, x0, y0, z1, r, g, b, a, -1, 0, 0);
        edge(buf, mat, pose, x0, y0, z1, x0, y0, z0, r, g, b, a, 0, 0, -1);
        // Top rectangle
        edge(buf, mat, pose, x0, y1, z0, x1, y1, z0, r, g, b, a, 1, 0, 0);
        edge(buf, mat, pose, x1, y1, z0, x1, y1, z1, r, g, b, a, 0, 0, 1);
        edge(buf, mat, pose, x1, y1, z1, x0, y1, z1, r, g, b, a, -1, 0, 0);
        edge(buf, mat, pose, x0, y1, z1, x0, y1, z0, r, g, b, a, 0, 0, -1);
        // Vertical edges
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
