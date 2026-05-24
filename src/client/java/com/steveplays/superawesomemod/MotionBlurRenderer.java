package com.steveplays.superawesomemod;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.steveplays.superawesomemod.mixin.PostChainAccessor;
import com.steveplays.superawesomemod.mixin.PostPassAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies motion blur by blending the current frame with the previous frame.
 * Uses Minecraft's built-in PostChain system instead of Satin.
 */
public final class MotionBlurRenderer {

    private MotionBlurRenderer() {}

    private static final Identifier POST_EFFECT_ID =
            Identifier.fromNamespaceAndPath("superawesomemod", "motion_blur");

    private static PostChain postChain;
    private static float currentBlur = -1;

    public static void register() {
        // Rendering is handled via GameRendererMotionBlurMixin — nothing to register here.
    }

    /**
     * Called from {@code GameRendererMotionBlurMixin} after entity outlines,
     * right before vanilla post-effects and GUI rendering.
     */
    public static void onRenderPost(CrossFrameResourcePool resourcePool) {
        if (!MotionBlurData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Load (or re-acquire after shader reload) the PostChain.
        PostChain chain = mc.getShaderManager().getPostChain(
                POST_EFFECT_ID,
                Set.of(PostChain.MAIN_TARGET_ID)
        );
        if (chain == null) return;

        // Detect shader reload: ShaderManager returns a new instance after F3+T.
        if (chain != postChain) {
            postChain = chain;
            currentBlur = -1; // force uniform update
        }

        // Update the BlendFactor uniform when the strength changes.
        float alpha = MotionBlurData.getAlpha();
        if (alpha != currentBlur) {
            updateBlendFactor(alpha);
            currentBlur = alpha;
        }

        postChain.process(mc.getMainRenderTarget(), resourcePool);
    }

    private static void updateBlendFactor(float alpha) {
        List<PostPass> passes = ((PostChainAccessor) postChain).superawesomemod$getPasses();
        if (passes.isEmpty()) return;

        // The first pass is the motion-blur blend pass that owns MotionBlurConfig.
        PostPass blendPass = passes.get(0);
        Map<String, GpuBuffer> uniforms =
                ((PostPassAccessor) blendPass).superawesomemod$getCustomUniforms();

        GpuBuffer old = uniforms.get("MotionBlurConfig");
        if (old != null && !old.isClosed()) {
            old.close();
        }

        // Pack a single float into a 16-byte buffer (std140 alignment).
        ByteBuffer buf = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
        buf.putFloat(0, alpha);

        GpuBuffer replacement = RenderSystem.getDevice().createBuffer(
                () -> "MotionBlurConfig",
                GpuBuffer.USAGE_UNIFORM,
                buf
        );
        uniforms.put("MotionBlurConfig", replacement);
    }
}
