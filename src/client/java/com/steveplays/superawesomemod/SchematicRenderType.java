package com.steveplays.superawesomemod;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Custom render types for the schematic overlay.
 * Ghost mode uses filled quads; verifier/outline mode uses lines.
 */
public final class SchematicRenderType {

    // Translucent filled quads for ghost block overlay
    private static final RenderPipeline GHOST_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("superawesomemod", "schematic_ghost"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withCull(false)
        .build();

    public static final RenderType SCHEMATIC_GHOST = RenderType.create(
        "schematic_ghost",
        RenderSetup.builder(GHOST_PIPELINE).createRenderSetup()
    );

    // Wireframe lines for verifier / outline mode
    private static final RenderPipeline OUTLINE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("superawesomemod", "schematic_outline"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build();

    public static final RenderType SCHEMATIC_OUTLINE = RenderType.create(
        "schematic_outline",
        RenderSetup.builder(OUTLINE_PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private SchematicRenderType() {}

    /** Force class loading so render pipelines are registered. */
    public static void touch() {}
}
