package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.steveplays.superawesomemod.FullColorArmorTint;
import com.steveplays.superawesomemod.FullColorTinted;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Full Colour, armour half — part one: publishes the tint for the duration of one
 * entity's armour submission.
 *
 * <p>Armour matters rather than being a nice-to-have. A fully kitted player is mostly
 * armour, so tinting only the body would barely show.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class FullColorArmorMixin {

    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At("HEAD")
    )
    private void superawesomemod$beginArmourTint(PoseStack poseStack, SubmitNodeCollector collector,
                                                 int light, HumanoidRenderState state,
                                                 float yRot, float xRot, CallbackInfo ci) {
        FullColorArmorTint.set(((FullColorTinted) state).superawesomemod$getFullColorTint());
    }

    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At("RETURN")
    )
    private void superawesomemod$endArmourTint(PoseStack poseStack, SubmitNodeCollector collector,
                                               int light, HumanoidRenderState state,
                                               float yRot, float xRot, CallbackInfo ci) {
        FullColorArmorTint.clear();
    }
}
