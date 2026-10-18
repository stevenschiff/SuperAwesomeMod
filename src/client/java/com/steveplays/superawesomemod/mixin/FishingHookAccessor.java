package com.steveplays.superawesomemod.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the hook's synced "biting" flag.
 *
 * <p>The server tells every client when a fish is on the line — that is how the bobber
 * animates — so the bite is a fact we are given rather than something to infer from a
 * splash sound or the bobber's vertical speed. The accessor is private and static, hence
 * this.
 */
@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("DATA_BITING")
    static EntityDataAccessor<Boolean> superawesomemod$dataBiting() {
        throw new AssertionError();
    }
}
