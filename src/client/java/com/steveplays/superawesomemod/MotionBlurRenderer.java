package com.steveplays.superawesomemod;

import net.minecraft.resources.Identifier;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

/**
 * Applies motion blur using Satin's post-processing shader pipeline.
 * Blends the current frame with the previous frame using a configurable BlendFactor.
 */
public final class MotionBlurRenderer {

    private MotionBlurRenderer() {}

    private static final ManagedShaderEffect MOTION_BLUR = ShaderEffectManager.getInstance()
            .manage(Identifier.fromNamespaceAndPath("motionblur", "shaders/post/motion_blur.json"),
                    shader -> shader.setUniformValue("BlendFactor", MotionBlurData.getAlpha()));

    private static float currentBlur;

    /** Call once during client init to register the Satin render callback. */
    public static void register() {
        ShaderEffectRenderCallback.EVENT.register(deltaTick -> {
            if (MotionBlurData.isEnabled()) {
                float alpha = MotionBlurData.getAlpha();
                if (currentBlur != alpha) {
                    MOTION_BLUR.setUniformValue("BlendFactor", alpha);
                    currentBlur = alpha;
                }
                MOTION_BLUR.render(deltaTick);
            }
        });
    }
}
