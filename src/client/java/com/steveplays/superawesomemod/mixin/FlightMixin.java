package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FlightData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flight: allows the player to fly in survival mode by overriding movement
 * each tick. Applies velocity based on WASD/Space/Shift input and the
 * player's look direction, then zeros out gravity effects.
 */
@Mixin(LocalPlayer.class)
public abstract class FlightMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void superawesomemod$applyFlight(CallbackInfo ci) {
        if (!FlightData.isEnabled()) return;

        LocalPlayer self = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        Options o = mc.options;
        float forward = (o.keyUp.isDown()    ? 1f : 0f) - (o.keyDown.isDown()  ? 1f : 0f);
        float strafeR = (o.keyRight.isDown() ? 1f : 0f) - (o.keyLeft.isDown()  ? 1f : 0f);
        float vert    = (o.keyJump.isDown()  ? 1f : 0f) - (o.keyShift.isDown() ? 1f : 0f);

        float yawRad = (float) Math.toRadians(self.getYRot());
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);

        double dx = -forward * sinY - strafeR * cosY;
        double dz =  forward * cosY - strafeR * sinY;

        // Normalize horizontal so diagonal isn't faster
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz > 1.0) { dx /= horiz; dz /= horiz; }

        float speed = FlightData.getSpeed();
        self.setDeltaMovement(new Vec3(dx * speed, vert * speed, dz * speed));

        // Prevent fall damage accumulation
        self.fallDistance = 0f;
    }
}
