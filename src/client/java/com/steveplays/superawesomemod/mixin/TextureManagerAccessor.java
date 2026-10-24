package com.steveplays.superawesomemod.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TickableTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * The set of textures Minecraft ticks. Every {@code TextureAtlas} is in here, which is a
 * shorter route to them than going through {@code ModelManager}'s private atlas manager.
 */
@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

    @Accessor("tickableTextures")
    Set<TickableTexture> superawesomemod$tickableTextures();
}
