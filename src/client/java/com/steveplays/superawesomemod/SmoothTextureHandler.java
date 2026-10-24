package com.steveplays.superawesomemod;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.steveplays.superawesomemod.mixin.TextureAtlasAccessor;
import com.steveplays.superawesomemod.mixin.TextureManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Upscales animated textures in time.
 *
 * <p>Minecraft advances a sprite's animation once per client tick and the driver is gated to
 * tick boundaries — {@code if (pendingTicks > 0 && isLevelRunningNormally())} in
 * {@code Minecraft.runTick} — so an animation shows 20 states a second however fast the
 * display runs. This pass runs once per <em>rendered</em> frame and writes the frame that
 * falls between the two the pack actually drew.
 *
 * <p>The animation clock here is its own, read from the wall clock at 50 ms per tick. That is
 * the same rate vanilla advances at, so nothing speeds up or drifts; it just lands on
 * fractional positions that an int {@code subFrame} cannot express. Vanilla still does its own
 * 20 Hz write earlier in the frame, and this pass overwrites it afterwards, so what reaches
 * the screen is always the interpolated image.
 *
 * <p>Only mip level 0 is rewritten. Higher mips keep vanilla's 20 Hz cadence, so a surface far
 * enough away to be mipped still steps — worth knowing, but items in the GUI and anything
 * nearby use mip 0.
 */
public final class SmoothTextureHandler {

    private static final long MS_PER_TICK = 50L;

    /** Per sprite, everything needed to build its in-between frame. */
    private static final Map<SpriteContents, SpriteAnimation> CACHE = new IdentityHashMap<>();
    /** Sprites we have already failed on, so a bad one is not retried every frame. */
    private static final Map<SpriteContents, Boolean> SKIP = new IdentityHashMap<>();

    private static boolean wasEnabled = false;
    /** Logged once per enable, so the log can tell "working" apart from "never switched on". */
    private static boolean reportedThisRun = false;

    private SmoothTextureHandler() {}

    public static void onRenderFrame(Minecraft client) {
        if (!SmoothTextureData.isEnabled()) {
            if (wasEnabled) {
                // Leaving the feature behind should leave no state behind either.
                clear();
                wasEnabled = false;
                reportedThisRun = false;
            }
            return;
        }
        wasEnabled = true;

        if (client.getTextureManager() == null) return;
        // Vanilla stops advancing animations while the game is paused, so this must too —
        // otherwise fire would keep flowing behind the pause menu in singleplayer.
        if (client.isPaused()) return;

        long now = Util.getMillis();
        for (TextureAtlas atlas : atlases(client)) {
            if (!inScope(atlas.location())) continue;
            drawAtlas(atlas, now);
        }

        if (!reportedThisRun) {
            reportedThisRun = true;
            int moving = 0;
            for (SpriteAnimation anim : CACHE.values()) {
                for (boolean t : anim.trusted) if (t) { moving++; break; }
            }
            SuperAwesomeMod.LOGGER.info(
                "[SmoothTextures] Interpolating {} animated sprites ({} with usable motion, {} skipped)",
                CACHE.size(), moving, SKIP.size());
        }
    }

    private static List<TextureAtlas> atlases(Minecraft client) {
        List<TextureAtlas> found = new ArrayList<>();
        var tickable = ((TextureManagerAccessor) client.getTextureManager())
                .superawesomemod$tickableTextures();
        for (TickableTexture t : tickable) {
            if (t instanceof TextureAtlas atlas) found.add(atlas);
        }
        return found;
    }

    private static boolean inScope(Identifier location) {
        return switch (SmoothTextureData.getScope()) {
            case EVERYTHING -> true;
            case BLOCKS -> TextureAtlas.LOCATION_BLOCKS.equals(location);
            case ITEMS -> TextureAtlas.LOCATION_ITEMS.equals(location);
        };
    }

    private static void drawAtlas(TextureAtlas atlas, long now) {
        GpuTexture target = atlas.getTexture();
        if (target == null) return;

        List<TextureAtlasSprite> sprites;
        try {
            sprites = ((TextureAtlasAccessor) atlas).superawesomemod$sprites();
        } catch (RuntimeException e) {
            return;
        }
        if (sprites == null || sprites.isEmpty()) return;

        var encoder = RenderSystem.getDevice().createCommandEncoder();

        for (TextureAtlasSprite sprite : sprites) {
            SpriteContents contents = sprite.contents();
            if (contents == null || !contents.isAnimated()) continue;
            if (SKIP.containsKey(contents)) continue;

            SpriteAnimation anim = CACHE.get(contents);
            if (anim == null) {
                anim = SpriteAnimation.of(contents);
                if (anim == null) {
                    SKIP.put(contents, Boolean.TRUE);
                    continue;
                }
                CACHE.put(contents, anim);
            }

            // Where the animation sits right now, as a step plus a fraction of it.
            long cycleMs = (long) anim.totalTicks * MS_PER_TICK;
            if (cycleMs <= 0) continue;
            long intoCycle = Math.floorMod(now, cycleMs);

            int step = 0;
            long consumed = 0;
            for (int i = 0; i < anim.stepTicks.length; i++) {
                long stepMs = (long) anim.stepTicks[i] * MS_PER_TICK;
                if (intoCycle < consumed + stepMs) { step = i; break; }
                consumed += stepMs;
            }
            long stepMs = (long) anim.stepTicks[step] * MS_PER_TICK;
            float t = stepMs <= 0 ? 0f : (float) (intoCycle - consumed) / stepMs;
            t = Math.clamp(t, 0f, 0.9999f);

            NativeImage image = anim.synthesise(step, t);
            encoder.writeToTexture(target, image, 0, 0,
                    sprite.getX(), sprite.getY(),
                    anim.frameWidth, anim.frameHeight, 0, 0);
        }
    }

    /** Dropped on resource reload — the cached images belong to sprites that are now closed. */
    public static void clear() {
        for (SpriteAnimation anim : CACHE.values()) {
            anim.close();
        }
        CACHE.clear();
        SKIP.clear();
    }
}
