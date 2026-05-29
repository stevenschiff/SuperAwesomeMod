package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

/**
 * HUD overlay showing schematic info: name, layer indicator, verifier summary.
 * Follows the same pattern as {@link ArmorHudOverlay}.
 */
public final class SchematicOverlay {

    private SchematicOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(SchematicOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!SchematicData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        int x = 4;
        int y = 4;
        int lineHeight = 10;
        int color = 0xFFFFFF;
        int dimColor = 0xAAAAAA;

        // Schematic name
        String name = placement.getSchematic().getName();
        graphics.drawString(mc.font, "Schematic: " + name, x, y, color);
        y += lineHeight;

        // Position
        BlockPos origin = placement.getOrigin();
        graphics.drawString(mc.font,
            "Pos: " + origin.getX() + ", " + origin.getY() + ", " + origin.getZ()
            + " | Rot: " + placement.getRotationName()
            + (placement.isMirror() ? " | Mirror" : ""),
            x, y, dimColor);
        y += lineHeight;

        // Layer mode
        if (SchematicData.isLayerMode()) {
            int layer = SchematicData.getCurrentLayer();
            int maxLayer = SchematicData.getMaxLayer();
            graphics.drawString(mc.font,
                "Layer " + layer + " / " + maxLayer + "  (PgUp/PgDn)",
                x, y, 0xFFFF55);
            y += lineHeight;
        }

        // Verifier summary (when in verifier mode)
        if (SchematicData.getRenderMode() == 1) {
            int correct = SchematicVerifier.getCorrectCount();
            int wrong = SchematicVerifier.getWrongCount();
            int missing = SchematicVerifier.getMissingCount();
            int extra = SchematicVerifier.getExtraCount();

            graphics.drawString(mc.font, "Verifier:", x, y, color);
            y += lineHeight;
            graphics.drawString(mc.font, "  Correct: " + correct, x, y, 0x55FF55);
            y += lineHeight;
            graphics.drawString(mc.font, "  Wrong: " + wrong, x, y, 0xFF5555);
            y += lineHeight;
            graphics.drawString(mc.font, "  Missing: " + missing, x, y, 0xFFFF55);
            y += lineHeight;
            graphics.drawString(mc.font, "  Extra: " + extra, x, y, 0xFF55FF);
        }
    }
}
