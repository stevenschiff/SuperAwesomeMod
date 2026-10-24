package com.steveplays.superawesomemod.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** The atlas keeps its sprite list private; we need it to find the animated ones. */
@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {

    @Accessor("sprites")
    List<TextureAtlasSprite> superawesomemod$sprites();
}
