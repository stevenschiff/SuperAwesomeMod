package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.steveplays.superawesomemod.ItemPhysicsData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Shadow @Final private RandomSource random;

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void superawesomemod$flatItemPhysics(
            ItemEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState cameraState,
            CallbackInfo ci) {
        if (!ItemPhysicsData.isEnabled()) return;
        if (state.item.isEmpty()) {
            ci.cancel();
            return;
        }

        poseStack.pushPose();

        AABB bounds = state.item.getModelBoundingBox();
        float depth = (float) bounds.getZsize();
        boolean isFlatItem = depth < 0.0625f;

        // Stable random Y rotation per entity so items don't all face the same way
        random.setSeed(state.seed);
        float yRot = random.nextFloat() * 360.0f;

        if (isFlatItem) {
            // Flat sprite item (sword, stick, etc.): lay face-up on the ground
            poseStack.translate(0.0f, 0.003f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        } else {
            // 3D block item (cobblestone, chest, etc.): sit on ground, no bob/spin
            float groundY = (float) (-bounds.minY);
            poseStack.translate(0.0f, groundY, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        }

        // Render item model(s) using the public static helper
        ItemEntityRenderer.submitMultipleFromCount(
                poseStack, collector, state.lightCoords, state, random, bounds);

        poseStack.popPose();
        ci.cancel();
    }
}
