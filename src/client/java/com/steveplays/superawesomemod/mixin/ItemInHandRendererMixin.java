package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.steveplays.superawesomemod.OldPvpData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void renderItem(LivingEntity entity, ItemStack stack,
            ItemDisplayContext context, PoseStack poseStack,
            SubmitNodeCollector collector, int light);

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void superawesomemod$swordBlockingVisual(
            AbstractClientPlayer player, float partialTicks, float pitch,
            InteractionHand hand, float swingProgress, ItemStack itemStack,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int combinedLight,
            CallbackInfo ci) {
        if (!OldPvpData.isBlockingEnabled()) return;
        if (itemStack.isEmpty() || !itemStack.is(ItemTags.SWORDS)) return;
        if (player.isScoping()) return;

        boolean useKeyHeld = minecraft.options.keyUse.isDown();
        boolean notActuallyUsing = !player.isUsingItem();

        if (!useKeyHeld || !notActuallyUsing) return;

        // Mark as custom blocking for swing-while-blocking detection
        if (hand == InteractionHand.MAIN_HAND) {
            OldPvpData.setCustomBlocking(true);
        }

        boolean isMainHand = (hand == InteractionHand.MAIN_HAND);
        HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRight = (arm == HumanoidArm.RIGHT);
        int side = isRight ? 1 : -1;

        poseStack.pushPose();

        // Apply standard arm positioning
        applyItemArmTransform(poseStack, arm, equipProgress);

        // Apply 1.7 sword blocking transforms (classic diagonal block pose)
        poseStack.translate(side * -0.14142136f, 0.08f, 0.14142136f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-102.25f));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 13.365f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 78.05f));

        // Render the sword in the blocking pose
        ItemDisplayContext ctx = isRight
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        renderItem(player, itemStack, ctx, poseStack, collector, combinedLight);

        poseStack.popPose();
        ci.cancel();
    }
}
