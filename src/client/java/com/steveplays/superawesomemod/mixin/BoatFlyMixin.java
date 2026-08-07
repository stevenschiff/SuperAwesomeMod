package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.BoatFlyData;
import com.steveplays.superawesomemod.ModKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Boat Fly: allows boats to fly when the player is riding them.
 * Uses Space/Shift for vertical movement, WASD for horizontal,
 * with flight physics similar to player flight.
 */
@Mixin(AbstractBoat.class)
public abstract class BoatFlyMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void superawesomemod$boatFly(CallbackInfo ci) {
        if (!BoatFlyData.isEnabled()) return;

        AbstractBoat boat = (AbstractBoat) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        // Only apply when the local player is the driver
        if (mc.player == null || !boat.hasPassenger(mc.player)
                || boat.getControllingPassenger() != mc.player) {
            return;
        }
        if (mc.screen != null) return;

        Options o = mc.options;
        float forward = (o.keyUp.isDown()    ? 1f : 0f) - (o.keyDown.isDown()  ? 1f : 0f);
        float strafeR = (o.keyRight.isDown() ? 1f : 0f) - (o.keyLeft.isDown()  ? 1f : 0f);
        // Descend on its own keybind (Right Shift by default) — sneak dismounts the boat.
        float vert    = (o.keyJump.isDown()  ? 1f : 0f)
                      - (ModKeybindings.boatFlyDown.isDown() ? 1f : 0f);

        float yawRad = (float) Math.toRadians(boat.getYRot());
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);

        double dx = -forward * sinY - strafeR * cosY;
        double dz =  forward * cosY - strafeR * sinY;

        // Normalize horizontal so diagonal isn't faster
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz > 1.0) { dx /= horiz; dz /= horiz; }

        float speed = BoatFlyData.getSpeed();
        boat.setDeltaMovement(new Vec3(dx * speed, vert * speed, dz * speed));

        // Prevent the boat from taking fall damage
        boat.fallDistance = 0f;
    }
}
