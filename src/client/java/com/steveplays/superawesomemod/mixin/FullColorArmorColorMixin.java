package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FullColorArmorTint;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Full Colour, armour half — part two: the single choke point where every armour
 * layer's colour is decided. Its return value becomes the {@code color} argument of
 * each armour {@code submitModel} call, so overriding it here covers helmet through
 * boots, dyed or not, in one place.
 *
 * <p>The tint is never 0 by construction — {@code renderLayers} treats a colour of 0
 * as "skip this layer", and a zero here would make armour vanish rather than tint.
 * Every entry in {@code CombatHitboxData.COLOR_ARGB} is opaque, which is what keeps
 * that safe.
 */
@Mixin(EquipmentLayerRenderer.class)
public abstract class FullColorArmorColorMixin {

    @Inject(
        method = "getColorForLayer(Lnet/minecraft/client/resources/model/EquipmentClientInfo$Layer;I)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void superawesomemod$tintArmour(EquipmentClientInfo.Layer layer, int dyeColor,
                                                   CallbackInfoReturnable<Integer> cir) {
        int tint = FullColorArmorTint.get();
        if (tint != 0) cir.setReturnValue(tint);
    }
}
