package com.steveplays.superawesomemod;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class XrayLineRenderType {

    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation("superawesomemod:lines_xray")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    public static final RenderType LINES_XRAY = RenderType.create(
        "superawesomemod:lines_xray",
        RenderSetup.builder(PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private XrayLineRenderType() {}

    public static void touch() {}
}
