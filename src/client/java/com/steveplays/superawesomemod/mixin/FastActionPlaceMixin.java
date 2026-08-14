package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FastActionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fast Place: clears the 4-tick cooldown vanilla puts between held right-clicks,
 * so holding use places blocks every tick instead of every fifth one.
 *
 * <p>Only while a block is held. {@code rightClickDelay} gates nothing but the
 * held-button repeat path, so clearing it unconditionally doesn't make single
 * clicks any faster — it just empties a stack of rockets, pearls or golden apples
 * the instant you hold right-click. Fast placement is wanted for building and
 * nowhere else, so the check is on the item rather than on the delay.
 */
@Mixin(Minecraft.class)
public abstract class FastActionPlaceMixin {

    @Shadow private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void superawesomemod$fastPlace(CallbackInfo ci) {
        if (!FastActionData.isFastPlace()) return;

        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) return;

        this.rightClickDelay = 0;
    }
}
