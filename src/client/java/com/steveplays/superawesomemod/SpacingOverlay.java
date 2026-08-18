package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The live half of the spacing trainer: what your gap is <em>right now</em>.
 *
 * <p>This is the part that actually teaches. Nobody can feel the difference between
 * 2.2 and 2.8 blocks unaided, but with a number and a colour attached the distinction
 * becomes learnable inside one session. The bar carries tick marks at the band edges
 * so it teaches where the bands are, not just where you currently sit.
 */
@SuppressWarnings("deprecation")
public final class SpacingOverlay {

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 4;
    private static final long FLASH_MS = 1000L;

    private static final Identifier CROSSHAIR_SPRITE =
        Identifier.withDefaultNamespace("hud/crosshair");
    private static final int CROSSHAIR_SIZE = 15;

    private SpacingOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(SpacingOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!SpacingData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer self = mc.player;
        if (self == null || self.isSpectator() || mc.level == null) return;

        Entity target = pickTarget(mc, self);
        // Confirmed with the user: nothing on screen unless a player is close.
        if (target == null) return;

        double reach = CombatReach.attackReach(self);
        double gap = CombatReach.gap(self.getEyePosition(), target.getBoundingBox());
        SpacingData.Zone zone = SpacingData.zoneOf(gap, reach);

        if (SpacingData.isGradedCrosshair()) {
            drawGradedCrosshair(graphics, zone);
        }

        float scale = SpacingData.getScale() / 2.0f;
        int panelW = BAR_WIDTH;
        int anchorX = (int) (graphics.guiWidth() / 2.0f - (panelW * scale) / 2.0f);
        int anchorY = (int) (graphics.guiHeight() - 62 * scale);

        graphics.pose().pushMatrix();
        graphics.pose().translate((float) anchorX, (float) anchorY);
        graphics.pose().scale(scale, scale);

        if (SpacingData.isShowMeter()) {
            drawMeter(graphics, mc, gap, reach, zone);
        }
        if (SpacingData.isShowFlash()) {
            drawFlash(graphics, mc);
        }

        graphics.pose().popMatrix();
    }

    private static void drawMeter(GuiGraphics graphics, Minecraft mc,
                                  double gap, double reach, SpacingData.Zone zone) {
        String number = String.format("%.2f", gap);
        String label = zone.label;

        int numberW = mc.font.width(number);
        graphics.drawString(mc.font, number,
            (BAR_WIDTH - numberW) / 2, 0, zone.argb, true);

        int barY = mc.font.lineHeight + 2;
        graphics.fill(0, barY, BAR_WIDTH, barY + BAR_HEIGHT, 0xC0000000);

        int filled = (int) Math.round(BAR_WIDTH * Math.clamp(gap / reach, 0.0, 1.0));
        graphics.fill(0, barY, filled, barY + BAR_HEIGHT, zone.argb);

        // Band edges, so the bar teaches the zones rather than just a level.
        tick(graphics, SpacingData.BAND_INSIDE, reach, barY);
        tick(graphics, SpacingData.BAND_CLOSE, reach, barY);
        tick(graphics, SpacingData.BAND_GOOD, reach, barY);

        int labelW = mc.font.width(label);
        graphics.drawString(mc.font, label,
            (BAR_WIDTH - labelW) / 2, barY + BAR_HEIGHT + 2, zone.argb, true);
    }

    private static void tick(GuiGraphics graphics, double at, double reach, int barY) {
        if (at >= reach) return;
        int x = (int) Math.round(BAR_WIDTH * (at / reach));
        graphics.fill(x, barY - 1, x + 1, barY + BAR_HEIGHT + 1, 0xFFFFFFFF);
    }

    /** The gap of your last swing, fading out. */
    private static void drawFlash(GuiGraphics graphics, Minecraft mc) {
        long age = SpacingTracker.millisSinceLastSwing();
        if (age > FLASH_MS) return;

        double last = SpacingTracker.getLastGap();
        if (Double.isNaN(last)) return;

        int alpha = (int) (255 * (1.0 - (double) age / FLASH_MS));
        if (alpha <= 8) return;

        LocalPlayer self = mc.player;
        double reach = self == null ? 3.0 : CombatReach.attackReach(self);
        int rgb = SpacingData.zoneOf(last, reach).argb & 0x00FFFFFF;

        String text = "hit " + String.format("%.2f", last);
        int w = mc.font.width(text);
        graphics.drawString(mc.font, text,
            (BAR_WIDTH - w) / 2, -(mc.font.lineHeight + 2), (alpha << 24) | rgb, true);
    }

    /**
     * Re-blits the crosshair tinted by zone — the same trick
     * {@code CombatCrosshairOverlay} uses for its binary in/out-of-range tint, graded
     * across the bands instead. Feedback with nothing extra to read.
     */
    private static void drawGradedCrosshair(GuiGraphics graphics, SpacingData.Zone zone) {
        int x = (graphics.guiWidth() - CROSSHAIR_SIZE) / 2;
        int y = (graphics.guiHeight() - CROSSHAIR_SIZE) / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROSSHAIR_SPRITE,
            x, y, CROSSHAIR_SIZE, CROSSHAIR_SIZE, zone.argb);
    }

    /**
     * The crosshair target when it's a player, otherwise the nearest player inside
     * {@link SpacingData#NEARBY_RADIUS}. Returns null when nobody is close, which is
     * what keeps the HUD clear outside of fights.
     */
    private static Entity pickTarget(Minecraft mc, LocalPlayer self) {
        if (mc.crosshairPickEntity instanceof Player p && p != self && p.isAttackable()) {
            return p;
        }

        Vec3 eye = self.getEyePosition();
        Entity best = null;
        double bestSqr = SpacingData.NEARBY_RADIUS * SpacingData.NEARBY_RADIUS;

        for (Player p : mc.level.players()) {
            if (p == self || !p.isAttackable() || p.isSpectator()) continue;
            double d = CombatReach.gapSqr(eye, p.getBoundingBox());
            if (d < bestSqr) {
                bestSqr = d;
                best = p;
            }
        }
        return best;
    }
}
