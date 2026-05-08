package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("deprecation")
public final class CombatCrosshairOverlay {

    private static final Identifier CROSSHAIR_SPRITE =
        Identifier.withDefaultNamespace("hud/crosshair");
    private static final int RED_TINT = 0xFFFF0000;
    private static final int CROSSHAIR_SIZE = 15;

    private CombatCrosshairOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(CombatCrosshairOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics g, DeltaTracker tickCounter) {
        if (!CombatCrosshairData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.isSpectator()) return;

        Entity target = mc.crosshairPickEntity;
        if (target == null || !target.isAttackable()) return;

        // Draw the vanilla crosshair sprite again with a red tint on top of the
        // normal one. This effectively recolors the crosshair when aimed at a
        // target within attack range.
        int cx = (g.guiWidth() - CROSSHAIR_SIZE) / 2;
        int cy = (g.guiHeight() - CROSSHAIR_SIZE) / 2;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, CROSSHAIR_SPRITE,
            cx, cy, CROSSHAIR_SIZE, CROSSHAIR_SIZE, RED_TINT);
    }
}
