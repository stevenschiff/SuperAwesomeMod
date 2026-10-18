package com.steveplays.superawesomemod.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes Minecraft's private startUseItem so the fishing rod can be cast and reeled
 * from outside the keybind pump. Calling it produces exactly the same effect as a real
 * right-click.
 */
@Mixin(Minecraft.class)
public interface MinecraftUseInvoker {

    @Invoker("startUseItem")
    void superawesomemod$startUseItem();
}
