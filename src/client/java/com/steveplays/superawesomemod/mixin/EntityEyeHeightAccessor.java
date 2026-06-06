package com.steveplays.superawesomemod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for Entity's eyeHeight field, used to sync the entity's
 * eye height with the higher-crouch camera position so raycasts match the camera.
 */
@Mixin(Entity.class)
public interface EntityEyeHeightAccessor {

    @Accessor("eyeHeight")
    float superawesomemod$getEyeHeight();

    @Accessor("eyeHeight")
    void superawesomemod$setEyeHeight(float eyeHeight);
}
