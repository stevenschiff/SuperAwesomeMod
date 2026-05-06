package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodData;

@SuppressWarnings("deprecation")
public final class AppleSkinOverlay {

    private static final int ICON_W = 9;
    private static final int ICON_H = 9;
    private static final int ICON_GAP = 8;
    private static final int RIGHT_OFFSET = 91;
    private static final int Y_FROM_BOTTOM = 39;

    private static final Identifier FOOD_FULL_SPRITE = Identifier.withDefaultNamespace("hud/food_full");
    private static final Identifier FOOD_HALF_SPRITE = Identifier.withDefaultNamespace("hud/food_half");

    // ARGB tint applied to the re-drawn hunger sprite to mark saturation backing.
    // Bright yellow at ~70% alpha — reads as a glow on top of the meat icon.
    private static final int SATURATION_TINT = 0xB0FFE040;

    private AppleSkinOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(AppleSkinOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics g, DeltaTracker tickCounter) {
        if (!AppleSkinData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.isCreative() || p.isSpectator()) return;

        FoodData fd = p.getFoodData();
        float sat = fd.getSaturationLevel();
        if (sat <= 0f) return;

        int guiW = g.guiWidth();
        int guiH = g.guiHeight();
        int top = guiH - Y_FROM_BOTTOM;
        int rightEdge = guiW / 2 + RIGHT_OFFSET;

        // Mirror vanilla half/full icon convention: each icon represents 2 saturation
        // points, right-to-left. Re-render the actual hunger sprite (full or half)
        // with a yellow tint so the saturation overlay reads as a meat-shaped glow
        // instead of a flat gold rectangle.
        for (int i = 0; i < 10; i++) {
            float satOverIcon = sat - i * 2f;
            if (satOverIcon <= 0f) break;

            int iconX = rightEdge - i * ICON_GAP - ICON_W;
            Identifier sprite = satOverIcon >= 2f ? FOOD_FULL_SPRITE : FOOD_HALF_SPRITE;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, iconX, top, ICON_W, ICON_H, SATURATION_TINT);
        }
    }
}
