package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreeLookData;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraFreeLookMixin {

    @Shadow public abstract float xRot();
    @Shadow public abstract float yRot();
    @Shadow public abstract Vec3 position();
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void move(float forward, float up, float right);

    // Camera manages its own interpolated eye height — shadow it to recompute entity eye pos.
    @Shadow private float eyeHeight;

    @Inject(method = "setup", at = @At("TAIL"))
    private void applyFreeLookOffset(Level level, Entity entity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        float yOff = FreeLookData.getYawOffset();
        float pOff = FreeLookData.getPitchOffset();
        if (yOff == 0f && pOff == 0f) return;

        float finalPitch = Mth.clamp(this.xRot() + pOff, -90f, 90f);

        if (detached) {
            // Third-person: the camera has already been moved backward from the entity eye.
            // We need to snap back to the eye position, apply the offset rotation, then
            // re-do the backward move so the camera orbits around the player.

            // Reconstruct entity eye position the same way Camera.setup() does.
            double eyeX = Mth.lerp(partialTick, entity.xo, entity.getX());
            double eyeY = Mth.lerp(partialTick, entity.yo, entity.getY()) + this.eyeHeight;
            double eyeZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
            Vec3 eyePos = new Vec3(eyeX, eyeY, eyeZ);

            // Distance the camera was placed at (already wall-clipped by getMaxZoom).
            float dist = (float) this.position().distanceTo(eyePos);

            // Reset to eye, rotate with offset, orbit outward at the same distance.
            setPosition(eyePos);
            setRotation(this.yRot() + yOff, finalPitch);
            move(-dist, 0, 0);
        } else {
            // First-person: rotation alone is sufficient.
            setRotation(this.yRot() + yOff, finalPitch);
        }
    }
}
