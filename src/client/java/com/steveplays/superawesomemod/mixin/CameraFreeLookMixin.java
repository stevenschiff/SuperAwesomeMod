package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FreeLookData;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraFreeLookMixin {

    @Shadow public abstract float xRot();
    @Shadow public abstract float yRot();
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void applyFreeLookOffset(CallbackInfo ci) {
        float yOff = FreeLookData.getYawOffset();
        float pOff = FreeLookData.getPitchOffset();
        if (yOff == 0f && pOff == 0f) return;
        setRotation(this.yRot() + yOff, this.xRot() + pOff);
    }
}
