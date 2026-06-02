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

        AABB bounds = state.item.getModelBoundingBox();

        float zSize = (float) bounds.getZsize();
        boolean isFlat = zSize < 0.125f;

        // Only affect blocks (3D items); let vanilla handle flat items
        if (isFlat) return;

        poseStack.pushPose();

        // Stable random Y rotation per entity so blocks don't all face the same way
        random.setSeed(state.seed);
        float yRot = random.nextFloat() * 360.0f;

        // 3D blocks: sit upright on the ground, no bob or spin.
        float modelHeight = (float) bounds.getYsize();
        poseStack.translate(0.0f, modelHeight * 0.5f + 0.005f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        // Render item model(s) using the public static helper
        ItemEntityRenderer.submitMultipleFromCount(
                poseStack, collector, state.lightCoords, state, random, bounds);

        poseStack.popPose();
        ci.cancel();
    }
}
