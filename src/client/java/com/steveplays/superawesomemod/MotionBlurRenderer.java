package com.steveplays.superawesomemod;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Applies a Lunar-style motion blur by accumulating frames.
 * Each frame, the previous accumulation buffer is blended onto the current
 * framebuffer at a configurable alpha, creating a trailing/smear effect.
 */
public final class MotionBlurRenderer {

    private MotionBlurRenderer() {}

    private static int accumFbo = -1;
    private static int accumTexture = -1;
    private static int texWidth = 0;
    private static int texHeight = 0;
    private static boolean hasAccumFrame = false;

    /**
     * Called from the mixin after the world has been rendered.
     * Blends the stored previous frame onto the current frame, then stores the result.
     */
    public static void applyMotionBlur() {
        if (!MotionBlurData.isEnabled()) {
            if (accumFbo != -1) {
                cleanup();
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (width <= 0 || height <= 0) return;

        // Get the currently bound framebuffer (the main one after world render)
        int mainFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        // Create or resize the accumulation framebuffer
        if (accumFbo == -1 || texWidth != width || texHeight != height) {
            cleanup();
            createAccumBuffer(width, height);
        }

        if (hasAccumFrame) {
            // Blend the previous frame (accum texture) onto the main framebuffer
            float alpha = MotionBlurData.getAlpha();

            // Bind main framebuffer for drawing
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainFbo);

            // Save GL state
            boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);

            // Enable alpha blending and draw the accum texture as fullscreen quad
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ZERO
            );

            // Draw the accumulation texture over the current frame
            drawFullscreenQuad(accumTexture, width, height, alpha);

            // Restore blend state
            if (!blendWasEnabled) {
                GlStateManager._disableBlend();
            }
        }

        // Copy current main framebuffer into the accumulation buffer for next frame
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, accumFbo);
        GlStateManager._glBlitFrameBuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
        );

        // Restore main framebuffer as draw target
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainFbo);
        hasAccumFrame = true;
    }

    private static void drawFullscreenQuad(int textureId, int width, int height, float alpha) {
        // Save current state
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Set up orthographic projection for fullscreen quad
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        // Disable depth test for fullscreen overlay
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Bind the accumulation texture
        GlStateManager._bindTexture(textureId);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Set color with alpha for blending
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        // Draw fullscreen quad
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex2f(width, 0);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex2f(0, height);
        GL11.glEnd();

        // Restore state
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }

    private static void createAccumBuffer(int width, int height) {
        // Create texture for accumulation
        accumTexture = GL11.glGenTextures();
        GlStateManager._bindTexture(accumTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
            0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._bindTexture(0);

        // Create framebuffer and attach texture
        accumFbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, accumFbo);
        GlStateManager._glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D, accumTexture, 0
        );
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        texWidth = width;
        texHeight = height;
        hasAccumFrame = false;
    }

    public static void cleanup() {
        if (accumFbo != -1) {
            GlStateManager._glDeleteFramebuffers(accumFbo);
            accumFbo = -1;
        }
        if (accumTexture != -1) {
            GL11.glDeleteTextures(accumTexture);
            accumTexture = -1;
        }
        texWidth = 0;
        texHeight = 0;
        hasAccumFrame = false;
    }
}
