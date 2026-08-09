package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.BHopData;
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
 * B Hop: jumps again the moment we land and holds the chosen speed through the arc.
 *
 * <p>Runs at the tail of {@code aiStep}, after this tick's move, so {@code onGround}
 * already reflects the landing — setting jump velocity here launches us on the next
 * tick, which is what makes the hopping continuous. Horizontal velocity is rewritten
 * every tick rather than only on takeoff, so speed carries through the air instead of
 * decaying to vanilla air control.
 */
@Mixin(LocalPlayer.class)
public abstract class BHopMixin {

    /** Vanilla jump impulse. Kept low deliberately: hops stay flat and fast. */
    private static final double JUMP_VELOCITY = 0.42;

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void superawesomemod$bhop(CallbackInfo ci) {
        if (!BHopData.isEnabled()) return;
        // Flight writes delta movement at this same point; two features fighting over
        // it would give whichever mixin applied last.
        if (FlightData.isEnabled()) return;

        LocalPlayer self = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        if (self.isPassenger() || self.getAbilities().flying) return;
        // Water and lava have their own movement rules; overriding them just makes
        // swimming unusable.
        if (self.isInWater() || self.isInLava()) return;

        Options o = mc.options;
        float forward = (o.keyUp.isDown()    ? 1f : 0f) - (o.keyDown.isDown() ? 1f : 0f);
        float strafeR = (o.keyRight.isDown() ? 1f : 0f) - (o.keyLeft.isDown() ? 1f : 0f);
        // Standing still should stand still, not hop in place.
        if (forward == 0f && strafeR == 0f) return;

        float yawRad = (float) Math.toRadians(self.getYRot());
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);

        double dx = -forward * sinY - strafeR * cosY;
        double dz =  forward * cosY - strafeR * sinY;

        // Normalise so strafing diagonally isn't faster than running straight.
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz > 1.0) { dx /= horiz; dz /= horiz; }

        float speed = BHopData.getSpeed();
        Vec3 current = self.getDeltaMovement();
        double y = self.onGround()
            ? JUMP_VELOCITY + self.getJumpBoostPower()
            : current.y;

        self.setDeltaMovement(dx * speed, y, dz * speed);
    }
}
