package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreecamData;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Higher priority than CameraFreeLookMixin (default 1000) so freecam overrides
// any free-look offsets when both happen to be active.
@Mixin(value = Camera.class, priority = 1100)
public abstract class CameraFreecamMixin {

    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void superawesomemod$applyFreecam(Level level, Entity entity, boolean detached,
                                              boolean mirror, float partialTick, CallbackInfo ci) {
        if (!FreecamData.isEnabled()) return;
        // Position is updated at tick rate (20 Hz) — interpolate to render rate so
        // the camera glides between ticks instead of stepping. Rotation is already
        // sampled per-frame inside MouseHandler.turnPlayer, so use it raw.
        setPosition(new Vec3(
            FreecamData.getInterpolatedX(partialTick),
            FreecamData.getInterpolatedY(partialTick),
            FreecamData.getInterpolatedZ(partialTick)));
        setRotation(FreecamData.getYaw(), FreecamData.getPitch());
    }
}
