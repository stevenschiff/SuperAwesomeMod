package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.steveplays.superawesomemod.ShieldData;
import com.steveplays.superawesomemod.ShieldRenderFlag;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shield: shifts the first-person shield up or down, and marks the window so
 * {@link ShieldTransparencyMixin} knows the shield being drawn is ours.
 *
 * <p>The translate happens at HEAD, before vanilla's own arm transforms, so it acts in
 * view space — a straightforward "move it up/down the screen" rather than something
 * that twists with the arm animation.
 *
 * <p>The push is tracked in a field rather than assumed, because another mixin on this
 * same method can cancel it. That cannot currently collide (this only touches shields,
 * the sword-blocking mixin only cancels for swords) but an unbalanced pose stack
 * corrupts every later draw call in the frame, which is not a failure worth risking on
 * an assumption.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ShieldOffsetMixin {

    @Unique
    private boolean superawesomemod$shieldPushed = false;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void superawesomemod$shieldBegin(
            AbstractClientPlayer player, float partialTicks, float pitch,
            InteractionHand hand, float swingProgress, ItemStack itemStack,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int combinedLight,
            CallbackInfo ci) {

        this.superawesomemod$shieldPushed = false;
        if (!ShieldData.isEnabled()) return;
        if (itemStack.isEmpty() || !itemStack.is(Items.SHIELD)) return;

        ShieldRenderFlag.set(true);

        // "Used" means raised: for a shield, blocking is item use, and we check the
        // stack identity so the offhand shield doesn't follow a main-hand item's use.
        boolean blocking = player.isUsingItem() && player.getUseItem() == itemStack;
        float offset = ShieldData.offsetFor(blocking);
        if (offset == 0.0f) return;

        poseStack.pushPose();
        poseStack.translate(0.0f, offset, 0.0f);
        this.superawesomemod$shieldPushed = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void superawesomemod$shieldEnd(
            AbstractClientPlayer player, float partialTicks, float pitch,
            InteractionHand hand, float swingProgress, ItemStack itemStack,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int combinedLight,
            CallbackInfo ci) {

        ShieldRenderFlag.set(false);

        if (this.superawesomemod$shieldPushed) {
            poseStack.popPose();
            this.superawesomemod$shieldPushed = false;
        }
    }
}
