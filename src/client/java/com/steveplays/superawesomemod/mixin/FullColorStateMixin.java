package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.FullColorTinted;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Gives every living render state somewhere to carry its tint. */
@Mixin(LivingEntityRenderState.class)
public abstract class FullColorStateMixin implements FullColorTinted {

    @Unique
    private int superawesomemod$fullColorTint = 0;

    @Override
    public int superawesomemod$getFullColorTint() {
        return this.superawesomemod$fullColorTint;
    }

    @Override
    public void superawesomemod$setFullColorTint(int argb) {
        this.superawesomemod$fullColorTint = argb;
    }
}
