package com.steveplays.superawesomemod.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes Minecraft's private startAttack/startUseItem so the autoclicker can
 * trigger them from outside the keybind pump. Calling these synthesizes the
 * exact same effect as a real left/right mouse click.
 */
@Mixin(Minecraft.class)
public interface MinecraftAutoclickerInvoker {

    @Invoker("startAttack")
    boolean superawesomemod$startAttack();

    @Invoker("startUseItem")
    void superawesomemod$startUseItem();
}
