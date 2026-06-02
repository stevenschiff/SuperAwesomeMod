package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NametagRenderer {

    private NametagRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(NametagRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!NametagData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null) return;

        // Only show in third person (F5)
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        renderNametag(self, ctx.matrices(), camera, partialTick, mc);
    }

    private static void renderNametag(LocalPlayer player, PoseStack poseStack,
                                       Camera camera, float partialTick,
                                       Minecraft mc) {
        Font font = mc.font;
        Vec3 camPos = camera.position();

        double x = Mth.lerp(partialTick, player.xo, player.getX());
        double y = Mth.lerp(partialTick, player.yo, player.getY());
        double z = Mth.lerp(partialTick, player.zo, player.getZ());

        float entityHeight = player.getBbHeight();
        Component name = player.getDisplayName();

        poseStack.pushPose();
        poseStack.translate(
            (float)(x - camPos.x),
            (float)(y - camPos.y) + entityHeight + 0.5f,
            (float)(z - camPos.z)
        );

        // Billboard rotation: always face the camera
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot()));

        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        float textWidth = font.width(name);
        Matrix4f matrix = poseStack.last().pose();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Draw with semi-transparent background (matches vanilla nametag style)
        font.drawInBatch(
            name,
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
