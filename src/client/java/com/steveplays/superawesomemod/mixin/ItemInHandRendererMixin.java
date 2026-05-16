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

    @Shadow
    private void swingArm(float swingProgress, PoseStack poseStack, int direction, HumanoidArm arm) {
        throw new AssertionError();
    }

    @Shadow
    private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        throw new AssertionError();
    }

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

        // Base arm positioning (modern equivalent of 1.8 transformFirstPersonItem)
        applyItemArmTransform(poseStack, arm, equipProgress);

        // Enter legacy (1.8) coordinate space — the blocking transforms were
        // designed for the old system where transformFirstPersonItem applied
        // rotateY(45) and scale(0.4). This wrapper converts between modern
        // and legacy coordinate systems (same technique as Animatium).
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0f));
        poseStack.scale(0.4f, 0.4f, 0.4f);

        // Block-hitting: apply swing animation in legacy space
        if (swingProgress > 0.0f) {
            swingArm(swingProgress, poseStack, side, arm);
        }

        // 1.8 blocking pose (vanilla func_178103_d)
        poseStack.translate(side * -0.5f, 0.2f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 30.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-80.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 60.0f));

        // Exit legacy coordinate space
        poseStack.scale(2.5f, 2.5f, 2.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -45.0f));

        // Render the sword in the blocking pose
        ItemDisplayContext ctx = isRight
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        renderItem(player, itemStack, ctx, poseStack, collector, combinedLight);

        poseStack.popPose();
        ci.cancel();
    }

    /**
     * 1.7-style swing while using items: overlays the arm swing animation on top
     * of item-use animations (eating, drinking, bow, etc.) by applying the attack
     * transform after the base arm transform in the use-animation branches.
     * Matches Animatium's itemUsageSwinging behavior.
     *
     * This inject fires after every applyItemArmTransform call in renderArmWithItem.
     * The isUsingItem() guard ensures it only takes effect inside the use-animation
     * branches (eating, drinking, bow, etc.), not in the idle branch where vanilla
     * already calls applyItemArmAttackTransform.
     */
    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    shift = At.Shift.AFTER))
    private void superawesomemod$itemUsageSwinging(
            AbstractClientPlayer player, float partialTicks, float pitch,
            InteractionHand hand, float swingProgress, ItemStack itemStack,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int combinedLight,
            CallbackInfo ci) {
        if (!OldPvpData.isSwingWhileUsingEnabled()) return;
        if (!player.isUsingItem()) return;

        HumanoidArm arm = (hand == InteractionHand.MAIN_HAND)
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        this.applyItemArmAttackTransform(poseStack, arm, swingProgress);
    }
}
