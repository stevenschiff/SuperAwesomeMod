package com.steveplays.superawesomemod;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.List;

/**
 * Everything needed to synthesise an in-between frame for one animated sprite.
 *
 * <p>The frame sequence lives in {@code SpriteContents$AnimatedTexture}, which is
 * package-private, so it cannot be {@code @Shadow}ed from this package. Reflection is used
 * instead, and deliberately <b>by type and by record-component order, never by name</b> —
 * a mod ships remapped to intermediary, so a hard-coded {@code "frames"} would resolve in
 * the dev environment and fail in the built jar. Every field below is unique by type within
 * its class, and {@code FrameInfo} is a record, whose components the JLS guarantees are
 * returned in declaration order.
 *
 * <p>All of it is read once per sprite and cached, so reflection never touches a frame.
 */
public final class SpriteAnimation {

    /** Largest shift considered when matching one frame to the next, in pixels. */
    private static final int MAX_SHIFT = 6;
    /**
     * How much better than no-shift an offset must score to be believed. A pair that
     * genuinely changes shape rather than moving will not clear this, and falls back to a
     * plain cross-fade instead of being smeared into fake motion.
     */
    private static final double MIN_IMPROVEMENT = 0.12;

    private static Field animatedTextureField;
    private static Field framesField;
    private static Field byMipLevelField;
    private static Method frameIndexAccessor;
    private static Method frameTimeAccessor;
    private static boolean reflectionReady;
    private static boolean reflectionFailed;

    /** Frame sequence as image indices, and how long each step lasts in ticks. */
    final int[] stepImage;
    final int[] stepTicks;
    final int totalTicks;

    final int frameWidth;
    final int frameHeight;
    final int framesPerRow;
    final NativeImage source;

    /** Per step: the shift from this step's image to the next one's, and whether to trust it. */
    final int[] shiftX;
    final int[] shiftY;
    final boolean[] trusted;

    /** Reused output buffer, so a frame-rate loop allocates nothing. */
    private NativeImage scratch;

    private SpriteAnimation(int[] stepImage, int[] stepTicks, int frameWidth, int frameHeight,
                            int framesPerRow, NativeImage source) {
        this.stepImage = stepImage;
        this.stepTicks = stepTicks;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.framesPerRow = framesPerRow;
        this.source = source;

        int total = 0;
        for (int t : stepTicks) total += Math.max(1, t);
        this.totalTicks = Math.max(1, total);

        this.shiftX = new int[stepImage.length];
        this.shiftY = new int[stepImage.length];
        this.trusted = new boolean[stepImage.length];
        estimateShifts();
    }

    // --- construction ---------------------------------------------------

    /** Returns null when the sprite is not animated, or when the internals moved on us. */
    public static SpriteAnimation of(SpriteContents contents) {
        if (!contents.isAnimated()) return null;
        if (!ensureReflection()) return null;

        try {
            Object animated = animatedTextureField.get(contents);
            if (animated == null) return null;

            List<?> frames = (List<?>) framesField.get(animated);
            if (frames == null || frames.isEmpty()) return null;

            int[] stepImage = new int[frames.size()];
            int[] stepTicks = new int[frames.size()];
            for (int i = 0; i < frames.size(); i++) {
                Object frame = frames.get(i);
                stepImage[i] = (Integer) frameIndexAccessor.invoke(frame);
                stepTicks[i] = Math.max(1, (Integer) frameTimeAccessor.invoke(frame));
            }

            NativeImage[] mips = (NativeImage[]) byMipLevelField.get(contents);
            if (mips == null || mips.length == 0 || mips[0] == null) return null;
            NativeImage source = mips[0];

            int frameWidth = contents.width();
            int frameHeight = contents.height();
            if (frameWidth <= 0 || frameHeight <= 0) return null;

            // The frame grid is derived rather than read: frames tile the source image left
            // to right, top to bottom, at the sprite's own size.
            int framesPerRow = Math.max(1, source.getWidth() / frameWidth);

            return new SpriteAnimation(stepImage, stepTicks, frameWidth, frameHeight,
                                       framesPerRow, source);
        } catch (ReflectiveOperationException | ClassCastException e) {
            SuperAwesomeMod.LOGGER.warn("[SmoothTextures] Could not read animation for {}",
                    contents.name(), e);
            return null;
        }
    }

    private static boolean ensureReflection() {
        if (reflectionReady) return true;
        if (reflectionFailed) return false;

        try {
            Class<?> contentsClass = SpriteContents.class;

            // The only NativeImage[] field, and the only field whose type is a member class
            // of SpriteContents -- that is AnimatedTexture, whatever it is called at runtime.
            for (Field f : contentsClass.getDeclaredFields()) {
                Class<?> type = f.getType();
                if (type == NativeImage[].class) {
                    f.setAccessible(true);
                    byMipLevelField = f;
                } else if (type.getEnclosingClass() == contentsClass && !type.isEnum()) {
                    // AnimationState is created on demand and never stored, so the only
                    // member-class-typed field here is the animation description itself.
                    f.setAccessible(true);
                    animatedTextureField = f;
                }
            }
            if (byMipLevelField == null || animatedTextureField == null) {
                throw new NoSuchFieldException("byMipLevel / animatedTexture");
            }

            Class<?> animatedClass = animatedTextureField.getType();
            for (Field f : animatedClass.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    framesField = f;
                    break;
                }
            }
            if (framesField == null) throw new NoSuchFieldException("frames");

            // FrameInfo: a record of (index, time). Record components are returned in
            // declaration order, which is what makes this safe without names.
            Class<?> frameClass = null;
            for (Class<?> c : contentsClass.getDeclaredClasses()) {
                if (c.isRecord() && c.getRecordComponents().length == 2
                        && c.getRecordComponents()[0].getType() == int.class
                        && c.getRecordComponents()[1].getType() == int.class) {
                    frameClass = c;
                    break;
                }
            }
            if (frameClass == null) throw new NoSuchFieldException("FrameInfo");

            RecordComponent[] parts = frameClass.getRecordComponents();
            frameIndexAccessor = parts[0].getAccessor();
            frameTimeAccessor = parts[1].getAccessor();
            frameIndexAccessor.setAccessible(true);
            frameTimeAccessor.setAccessible(true);

            reflectionReady = true;
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            SuperAwesomeMod.LOGGER.error(
                "[SmoothTextures] Animation internals not where expected; feature disabled", e);
            reflectionFailed = true;
            return false;
        }
    }

    // --- motion estimation ----------------------------------------------

    /**
     * For each step, finds the shift that best lines its image up with the next one.
     * Searched vertically first because flame and lava animations scroll upward; the
     * horizontal term catches the rest.
     */
    private void estimateShifts() {
        // Cost here is steps x shifts x pixels, which on a 64x64 pack texture with 64 frames
        // would stall the frame it is first enabled on. Two bounds keep it cheap: the search
        // window never exceeds a quarter of the frame, and the match is scored on a sampled
        // grid rather than every pixel. Both only affect the offset estimate, never output.
        int window = Math.max(1, Math.min(MAX_SHIFT, Math.min(frameWidth, frameHeight) / 4));
        int stride = Math.max(1, Math.min(frameWidth, frameHeight) / 16);

        for (int i = 0; i < stepImage.length; i++) {
            int a = stepImage[i];
            int b = stepImage[(i + 1) % stepImage.length];

            double still = score(a, b, 0, 0, stride);
            if (still <= 0.0001) continue; // identical frames; nothing to move

            double best = still;
            int bestX = 0;
            int bestY = 0;
            for (int dy = -window; dy <= window; dy++) {
                for (int dx = -window; dx <= window; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    double s = score(a, b, dx, dy, stride);
                    if (s < best) { best = s; bestX = dx; bestY = dy; }
                }
            }

            double improvement = (still - best) / still;
            if (improvement >= MIN_IMPROVEMENT && (bestX != 0 || bestY != 0)) {
                shiftX[i] = bestX;
                shiftY[i] = bestY;
                trusted[i] = true;
            }
        }
    }

    /** Mean absolute difference between frame {@code b} and frame {@code a} shifted by (dx,dy). */
    private double score(int frameA, int frameB, int dx, int dy, int stride) {
        long sum = 0;
        int counted = 0;
        for (int y = 0; y < frameHeight; y += stride) {
            for (int x = 0; x < frameWidth; x += stride) {
                int ax = x - dx;
                int ay = y - dy;
                if (ax < 0 || ay < 0 || ax >= frameWidth || ay >= frameHeight) continue;
                int pa = pixel(frameA, ax, ay);
                int pb = pixel(frameB, x, y);
                sum += Math.abs(((pa >> 24) & 0xFF) - ((pb >> 24) & 0xFF))
                     + Math.abs(((pa >> 16) & 0xFF) - ((pb >> 16) & 0xFF))
                     + Math.abs(((pa >> 8) & 0xFF) - ((pb >> 8) & 0xFF))
                     + Math.abs((pa & 0xFF) - (pb & 0xFF));
                counted++;
            }
        }
        if (counted == 0) return Double.MAX_VALUE;
        return (double) sum / counted;
    }

    /** One pixel of a frame, by frame index and position inside it. */
    private int pixel(int frameIndex, int x, int y) {
        int fx = (frameIndex % framesPerRow) * frameWidth + x;
        int fy = (frameIndex / framesPerRow) * frameHeight + y;
        if (fx < 0 || fy < 0 || fx >= source.getWidth() || fy >= source.getHeight()) return 0;
        return source.getPixel(fx, fy);
    }

    /** One pixel of a frame, transparent outside the frame's own bounds. */
    private int clipped(int frameIndex, int x, int y) {
        if (x < 0 || y < 0 || x >= frameWidth || y >= frameHeight) return 0;
        return pixel(frameIndex, x, y);
    }

    /**
     * Samples a frame displaced by (dx,dy). Whole-pixel when Crisp pixels is on, which keeps
     * the art sharp; bilinear otherwise, which moves more smoothly at the cost of softening
     * the edges a pixel-art texture depends on.
     */
    private int sample(int frameIndex, int x, int y, float dx, float dy, boolean crisp) {
        if (crisp) {
            return clipped(frameIndex, x - Math.round(dx), y - Math.round(dy));
        }
        float sx = x - dx;
        float sy = y - dy;
        int x0 = (int) Math.floor(sx);
        int y0 = (int) Math.floor(sy);
        float fx = sx - x0;
        float fy = sy - y0;

        int top = lerpPixel(clipped(frameIndex, x0, y0), clipped(frameIndex, x0 + 1, y0), fx);
        int bottom = lerpPixel(clipped(frameIndex, x0, y0 + 1), clipped(frameIndex, x0 + 1, y0 + 1), fx);
        return lerpPixel(top, bottom, fy);
    }

    // --- frame synthesis -------------------------------------------------

    /**
     * Builds the image that sits {@code t} of the way from step {@code step} to the next,
     * into a buffer reused across calls.
     */
    public NativeImage synthesise(int step, float t) {
        if (scratch == null) {
            scratch = new NativeImage(frameWidth, frameHeight, false);
        }

        int a = stepImage[step];
        int b = stepImage[(step + 1) % stepImage.length];

        if (a == b) {
            // The sequence holds this image for more than one step; nothing to blend.
            copy(a);
            return scratch;
        }

        boolean motion = SmoothTextureData.getMode() == SmoothTextureData.Mode.MOTION
                && trusted[step];
        boolean crisp = SmoothTextureData.isCrispPixels();

        // Meet in the middle: the old frame travels forward, the new one comes back, so at
        // t = 0.5 both sit where the moving shape actually is rather than at either end.
        float fwdX = shiftX[step] * t;
        float fwdY = shiftY[step] * t;
        float backX = fwdX - shiftX[step];
        float backY = fwdY - shiftY[step];

        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {
                int pa = motion ? sample(a, x, y, fwdX, fwdY, crisp) : pixel(a, x, y);
                int pb = motion ? sample(b, x, y, backX, backY, crisp) : pixel(b, x, y);
                scratch.setPixel(x, y, lerpPixel(pa, pb, t));
            }
        }
        return scratch;
    }

    private void copy(int frameIndex) {
        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {
                scratch.setPixel(x, y, pixel(frameIndex, x, y));
            }
        }
    }

    /**
     * Blends two pixels with alpha premultiplied, so a transparent pixel contributes no
     * colour. Blending raw channels instead would drag whatever colour sits behind a
     * transparent texel into the result and halo the edges of the flame.
     */
    private static int lerpPixel(int pa, int pb, float t) {
        int aa = (pa >> 24) & 0xFF;
        int ab = (pb >> 24) & 0xFF;

        float outA = aa + (ab - aa) * t;
        if (outA < 0.5f) return 0;

        float wa = aa * (1f - t);
        float wb = ab * t;
        float w = wa + wb;
        if (w < 0.0001f) return 0;

        int r = Math.round((((pa >> 16) & 0xFF) * wa + ((pb >> 16) & 0xFF) * wb) / w);
        int g = Math.round((((pa >> 8) & 0xFF) * wa + ((pb >> 8) & 0xFF) * wb) / w);
        int b = Math.round(((pa & 0xFF) * wa + (pb & 0xFF) * wb) / w);

        return (Math.round(outA) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    public void close() {
        if (scratch != null) {
            scratch.close();
            scratch = null;
        }
    }
}
