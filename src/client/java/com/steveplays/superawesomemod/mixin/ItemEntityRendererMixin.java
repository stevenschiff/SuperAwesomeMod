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

        // Stable random Y rotation per entity so items don't all face the same way
        random.setSeed(state.seed);
        float yRot = random.nextFloat() * 360.0f;

        float zSize = (float) bounds.getZsize();
        boolean isFlat = zSize < 0.125f;

        if (isFlat) {
            // Flat items (tools, food, ingots, etc.): lay face-up on the ground.
            // -90 degrees around X maps the item face (+Z) to point upward (+Y).
            // Use the model's Z thickness (not Y height) for the offset since
            // that becomes the vertical extent after rotation.
            poseStack.translate(0.0f, zSize * 0.5f + 0.005f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-10.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        } else {
            // 3D items (blocks): sit upright on the ground, no bob or spin.
            float modelHeight = (float) bounds.getYsize();
            poseStack.translate(0.0f, modelHeight * 0.5f + 0.005f, 0.0f);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        // Render item model(s) using the public static helper
        ItemEntityRenderer.submitMultipleFromCount(
                poseStack, collector, state.lightCoords, state, random, bounds);

        poseStack.popPose();
        ci.cancel();
    }
}
