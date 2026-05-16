package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class MiniMapWaypointRenderer {

    private MiniMapWaypointRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(MiniMapWaypointRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!MiniMapData.isEnabled()) return;
        if (MiniMapData.getWaypoints().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();
        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();

        VertexConsumer buf = consumers.getBuffer(XrayLineRenderType.LINES_XRAY);

        for (MiniMapWaypoint wp : MiniMapData.getVisibleWaypoints()) {
            renderWaypoint(ctx, buf, matrix, pose, camera, camPos, player, wp, mc);
        }
    }

    private static void renderWaypoint(WorldRenderContext ctx, VertexConsumer buf,
                                        Matrix4f matrix, PoseStack.Pose pose,
                                        Camera camera, Vec3 camPos,
                                        LocalPlayer player, MiniMapWaypoint wp,
                                        Minecraft mc) {
        // Waypoint world position (at y=100 for visibility, or use a high fixed Y)
        float wx = (float) (wp.x() + 0.5 - camPos.x);
        float wz = (float) (wp.z() + 0.5 - camPos.z);

        // Calculate distance for display
        double distXZ = Math.sqrt(
            Math.pow(wp.x() + 0.5 - player.getX(), 2) +
            Math.pow(wp.z() + 0.5 - player.getZ(), 2)
        );

        // Render a vertical pillar line at the waypoint position
        // From y=0 to y=320 so it's always visible
        float yBot = (float) (64 - camPos.y);
        float yTop = (float) (320 - camPos.y);

        float r = wp.r();
        float g = wp.g();
        float b = wp.b();

        // Vertical pillar line
        edge(buf, matrix, pose, wx, yBot, wz, wx, yTop, wz, r, g, b, 1f, 0, 1, 0);

        // Cross at a visible height (player Y level + 20)
        float crossY = (float) (player.getY() + 20 - camPos.y);
        float crossSize = 1.0f;
        edge(buf, matrix, pose, wx - crossSize, crossY, wz, wx + crossSize, crossY, wz, r, g, b, 1f, 1, 0, 0);
        edge(buf, matrix, pose, wx, crossY, wz - crossSize, wx, crossY, wz + crossSize, r, g, b, 1f, 0, 0, 1);

        // Render text label using the font system
        renderLabel(ctx, camera, camPos, wp, distXZ, mc);
    }

    private static void renderLabel(WorldRenderContext ctx, Camera camera, Vec3 camPos,
                                     MiniMapWaypoint wp, double distance, Minecraft mc) {
        PoseStack poseStack = ctx.matrices();
        poseStack.pushPose();

        // Position the label above the waypoint at player eye level + 20
        double labelY = mc.player.getY() + 20;
        float wx = (float) (wp.x() + 0.5 - camPos.x);
        float wy = (float) (labelY - camPos.y);
        float wz = (float) (wp.z() + 0.5 - camPos.z);

        poseStack.translate(wx, wy + 1.5, wz);

        // Billboard rotation: face the camera
        float camYaw = camera.yRot();
        float camPitch = camera.xRot();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-camYaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(camPitch));

        // Scale based on distance (stays readable from far away)
        float scale = (float) Math.max(0.025f, Math.min(0.15f, distance * 0.005));
        poseStack.scale(-scale, -scale, scale);

        // Draw name and distance
        String label = wp.name() + " (" + (int) distance + "m)";
        int textColor = 0xFF000000 | wp.color();
        Matrix4f textMatrix = poseStack.last().pose();

        MultiBufferSource consumers = ctx.consumers();
        if (consumers != null) {
            mc.font.drawInBatch(
                Component.literal(label),
                -mc.font.width(label) / 2f, 0,
                textColor,
                false,
                textMatrix,
                consumers,
                net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH,
                0x40000000,
                15728880
            );
        }

        poseStack.popPose();
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
