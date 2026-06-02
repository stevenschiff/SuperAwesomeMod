package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class HealthIndicatorRenderer {

    private HealthIndicatorRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(HealthIndicatorRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!HealthIndicatorData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        ClientLevel level = mc.level;
        if (self == null || level == null) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (entity == self) continue;

            double distSq = self.distanceToSqr(entity);
            if (distSq > 64 * 64) continue; // max 64 blocks away

            renderHealthAbove(player, ctx.matrices(), camera, partialTick, mc);
        }
    }

    private static void renderHealthAbove(Player player, PoseStack poseStack,
                                           Camera camera, float partialTick,
                                           Minecraft mc) {
        Font font = mc.font;
        Vec3 camPos = camera.position();

        double x = Mth.lerp(partialTick, player.xo, player.getX());
        double y = Mth.lerp(partialTick, player.yo, player.getY());
        double z = Mth.lerp(partialTick, player.zo, player.getZ());

        float entityHeight = player.getBbHeight();

        poseStack.pushPose();
        poseStack.translate(
            (float)(x - camPos.x),
            (float)(y - camPos.y) + entityHeight + 0.3f,
            (float)(z - camPos.z)
        );

        // Billboard rotation: always face the camera
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot()));

        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        // Build hearts text from player health
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int fullHearts = (int)(health / 2);
        boolean halfHeart = (health % 2) >= 1;
        int totalHearts = (int)(maxHealth / 2);

        MutableComponent text = Component.empty();
        for (int i = 0; i < totalHearts; i++) {
            if (i < fullHearts || (i == fullHearts && halfHeart)) {
                text.append(Component.literal("\u2764")
                    .withStyle(style -> style.withColor(0xFF5555))); // red
            } else {
                text.append(Component.literal("\u2764")
                    .withStyle(style -> style.withColor(0x555555))); // dark gray
            }
        }

        float textWidth = font.width(text);
        Matrix4f matrix = poseStack.last().pose();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        font.drawInBatch(
            text,
            -textWidth / 2f, 0,
            0xFFFFFFFF,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0x40000000,
            15728880
        );
        bufferSource.endBatch();

        poseStack.popPose();
    }
}
