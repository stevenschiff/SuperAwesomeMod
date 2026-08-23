package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.ShieldData;
import com.steveplays.superawesomemod.ShieldRenderFlag;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Shield transparency.
 *
 * <p>Two things have to change together, and neither works alone. The shield normally
 * draws through {@code armorCutoutNoCull}, which does not blend — an alpha in the tint
 * would simply be ignored — so the render type is swapped for the translucent variant.
 * And the tint itself is a hardcoded {@code -1} (opaque white) at each submit call, so
 * the alpha has to be written into that argument.
 *
 * <p>Both targets are pinned to the owners the bytecode actually records — the
 * renderType call is compiled against {@code ShieldModel} rather than its declaring
 * class {@code Model}, so the redirect receiver must be ShieldModel to validate.
 *
 * <p>Scope: only while {@link ShieldRenderFlag} says we are drawing the first-person
 * shield, so shields on other players, dropped shields and item frames are untouched.
 *
 * <p>Known limit: a shield carrying a banner pattern submits those pattern layers
 * through {@code BannerRenderer}, which is not on this path, so the patterns stay
 * solid while the shield beneath them fades. Plain shields fade completely.
 */
@Mixin(ShieldSpecialRenderer.class)
public abstract class ShieldTransparencyMixin {

    @Redirect(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/object/equipment/ShieldModel;renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
        )
    )
    private RenderType superawesomemod$blendableShield(ShieldModel model, Identifier atlas) {
        if (superawesomemod$active()) {
            return RenderTypes.armorTranslucent(atlas);
        }
        return model.renderType(atlas);
    }

    @ModifyArg(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZZILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V"
        ),
        index = 8
    )
    private int superawesomemod$fadeShield(int tintedColor) {
        if (!superawesomemod$active()) return tintedColor;
        // Keep the original colour, replace only the alpha byte.
        return (ShieldData.alpha() << 24) | (tintedColor & 0x00FFFFFF);
    }

    private static boolean superawesomemod$active() {
        return ShieldRenderFlag.isActive() && ShieldData.isTintActive();
    }
}
