package com.steveplays.superawesomemod.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The backing texture, readable without throwing.
 *
 * <p>{@code AbstractTexture.getTexture()} raises {@code IllegalStateException} when nothing
 * has created the texture yet rather than returning null, and an atlas is registered as
 * tickable before it is uploaded. Reading the field directly lets a caller skip an atlas
 * that is not ready instead of crashing on it.
 */
@Mixin(AbstractTexture.class)
public interface AbstractTextureAccessor {

    @Accessor("texture")
    GpuTexture superawesomemod$textureOrNull();
}
