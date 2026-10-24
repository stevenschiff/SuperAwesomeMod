package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Drops the interpolation cache whenever resources reload.
 *
 * <p>The cache is keyed on {@code SpriteContents} identity and holds a scratch image per
 * sprite. A reload builds new sprites and closes the old ones, so without this the cache
 * would keep references to freed native memory and grow on every pack switch or F3+T.
 */
public final class SmoothTextureReload implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID =
        Identifier.fromNamespaceAndPath("superawesomemod", "smooth_textures");

    private SmoothTextureReload() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(new SmoothTextureReload());
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        SmoothTextureHandler.clear();
    }
}
