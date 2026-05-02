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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class CombatHitboxRenderer {

    private CombatHitboxRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(CombatHitboxRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!CombatHitboxData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        ClientLevel level = mc.level;
        if (self == null || level == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();
        Vec3 eye = self.getEyePosition();
        double reach = getAttackReach(self);
        double reachSqr = reach * reach;

        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();

        // Two passes — never cache both buffers at once. The world's MultiBufferSource routes
        // unknown render types through one shared BufferBuilder and ends the previous batch when
        // a new shared type is requested, which would invalidate a held VertexConsumer.
        drawPass(consumers.getBuffer(RenderTypes.lines()),         level, self, eye, reachSqr, matrix, pose, camPos, false);
        drawPass(consumers.getBuffer(XrayLineRenderType.LINES_XRAY), level, self, eye, reachSqr, matrix, pose, camPos, true);
    }

    private static void drawPass(VertexConsumer buffer, ClientLevel level, LocalPlayer self,
                                 Vec3 eye, double reachSqr, Matrix4f matrix, PoseStack.Pose pose,
                                 Vec3 camPos, boolean invisOnly) {
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == self) continue;
            if (!entity.isAttackable()) continue;
            if (entity.isInvisible() != invisOnly) continue;

            AABB box = entity.getBoundingBox();
            boolean inReach = nearestPointDistanceSqr(eye, box) <= reachSqr;

            float r = 1.0f;
            float g = inReach ? 0.0f : 1.0f;
            float b = inReach ? 0.0f : 1.0f;

            renderBoxLines(buffer, matrix, pose, box, camPos, r, g, b, 1.0f);
        }
    }

    private static double getAttackReach(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attr != null) return attr.getValue();
        return Player.DEFAULT_ENTITY_INTERACTION_RANGE;
    }

    private static double nearestPointDistanceSqr(Vec3 point, AABB box) {
        double dx = Math.max(0.0, Math.max(box.minX - point.x, point.x - box.maxX));
        double dy = Math.max(0.0, Math.max(box.minY - point.y, point.y - box.maxY));
        double dz = Math.max(0.0, Math.max(box.minZ - point.z, point.z - box.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    private static void renderBoxLines(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                                       AABB box, Vec3 cam,
                                       float r, float g, float b, float a) {
        float x0 = (float) (box.minX - cam.x);
        float y0 = (float) (box.minY - cam.y);
        float z0 = (float) (box.minZ - cam.z);
        float x1 = (float) (box.maxX - cam.x);
        float y1 = (float) (box.maxY - cam.y);
        float z1 = (float) (box.maxZ - cam.z);

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
        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        buf.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }
}
