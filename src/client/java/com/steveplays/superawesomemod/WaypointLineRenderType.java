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
 * Depth-ignoring lines, used only to draw your own map waypoints so a marker stays
 * visible behind terrain.
 *
 * <p>This was previously shared with the X-ray renderer and the hitbox see-through
 * pass. Both are gone, and it is kept deliberately narrow: waypoints are markers you
 * placed yourself, so drawing them through a wall reveals nothing the game was hiding
 * from you.
 */
public final class WaypointLineRenderType {

    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("superawesomemod", "lines_waypoint"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    public static final RenderType LINES_WAYPOINT = RenderType.create(
        "lines_waypoint",
        RenderSetup.builder(PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private WaypointLineRenderType() {}

    public static void touch() {}
}
