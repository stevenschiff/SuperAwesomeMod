package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreecamData;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses local player movement input while freecam is active so the body
 * stays put while the camera flies free. The Input record is replaced with
 * EMPTY (all-false) and the move vector is zeroed.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputFreecamMixin extends ClientInput {

    @Shadow protected Vec2 moveVector;

    @Inject(method = "tick", at = @At("TAIL"))
    private void superawesomemod$suppressInputDuringFreecam(CallbackInfo ci) {
        if (!FreecamData.isEnabled()) return;
        this.keyPresses = Input.EMPTY;
        this.moveVector = Vec2.ZERO;
    }
}
