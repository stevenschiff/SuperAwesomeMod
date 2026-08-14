package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.QuickThrowData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick Throw: removes the client's own 4-tick gap between right-clicks while a
 * throwable is held, so consecutive pearls, wind charges and gapples go out as fast
 * as you can click instead of once every 200ms.
 *
 * <p>What this is not: it does not make projectiles client-side, and it cannot make
 * ping behave like zero. The server spawns the pearl and the server performs the
 * teleport — a locally spawned one would be a ghost that moves nothing, and the real
 * one would still arrive a round trip later. {@code rightClickDelay} is the only part
 * of that chain we own, so it is the only part removed. On a 200ms connection this
 * wins back the 200ms we were adding ourselves, not the 200ms of network.
 */
@Mixin(Minecraft.class)
public abstract class QuickThrowMixin {

    @Shadow private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void superawesomemod$quickThrow(CallbackInfo ci) {
        if (!QuickThrowData.isEnabled()) return;

        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return;

        if (QuickThrowData.isQuickItem(player.getMainHandItem())
            || QuickThrowData.isQuickItem(player.getOffhandItem())) {
            this.rightClickDelay = 0;
        }
    }
}
