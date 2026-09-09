package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Toggles plus the review surface: session stats and a histogram of hit distances.
 * Hit-to-hit feedback lives on the HUD; this is where a practice run's progress shows.
 */
public class SpacingScreen extends Screen {

    private final Screen parent;

    public SpacingScreen(Screen parent) {
        super(Component.literal("Spacing Trainer"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;
        int left = cx - btnW - 6;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                SpacingData.setEnabled(!SpacingData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(left, cy - 60, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Live meter", SpacingData.isShowMeter()),
            btn -> {
                SpacingData.setShowMeter(!SpacingData.isShowMeter());
                btn.setMessage(sub("Live meter", SpacingData.isShowMeter()));
            }
        ).bounds(left, cy - 36, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Hit flash", SpacingData.isShowFlash()),
            btn -> {
                SpacingData.setShowFlash(!SpacingData.isShowFlash());
                btn.setMessage(sub("Hit flash", SpacingData.isShowFlash()));
            }
        ).bounds(left, cy - 12, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Graded crosshair", SpacingData.isGradedCrosshair()),
            btn -> {
                SpacingData.setGradedCrosshair(!SpacingData.isGradedCrosshair());
                btn.setMessage(sub("Graded crosshair", SpacingData.isGradedCrosshair()));
            }
        ).bounds(left, cy + 12, btnW, btnH).build());

        this.addRenderableWidget(new ScaleSlider(left, cy + 36, btnW, btnH, SpacingData.getScale()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Reset session"),
            btn -> SpacingTracker.reset()
        ).bounds(left, cy + 60, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(left, cy + 84, btnW, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(SpacingData.isEnabled()
            ? "Disable Spacing Trainer" : "Enable Spacing Trainer");
    }

    private static Component sub(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 84, 0xFFFFFF);

        boolean on = SpacingData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 72, on ? 0x55FF55 : 0xFF5555);

        drawStats(graphics, cx + 12, cy - 60);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawStats(GuiGraphics graphics, int x, int y) {
        int swings = SpacingTracker.getSwings();

        graphics.drawString(this.font, "Session", x, y, 0xFFFFFF, false);
        y += 12;

        if (swings == 0) {
            graphics.drawString(this.font, "No swings yet.", x, y, 0xAAAAAA, false);
            graphics.drawString(this.font, "Hit something to start.", x, y + 10, 0xAAAAAA, false);
            return;
        }

        graphics.drawString(this.font,
            String.format("Mean gap: %.2f", SpacingTracker.getMeanGap()), x, y, 0xFFFFFF, false);
        y += 10;
        graphics.drawString(this.font,
            String.format("Range: %.2f - %.2f", SpacingTracker.getMinGap(), SpacingTracker.getMaxGap()),
            x, y, 0xAAAAAA, false);
        y += 10;
        graphics.drawString(this.font,
            String.format("In good band: %.0f%%", SpacingTracker.getInBandPercent()),
            x, y, 0xFF55FF55, false);
        y += 10;
        graphics.drawString(this.font,
            String.format("Confirmed: %.0f%% of %d", SpacingTracker.getConfirmedPercent(), swings),
            x, y, 0xAAAAAA, false);
        y += 10;
        // Stated plainly: an unconfirmed swing is not proof of a reach failure.
        graphics.drawString(this.font, "(i-frames and shields also hide hits)",
            x, y, 0xFF888888, false);
        y += 16;

        drawHistogram(graphics, x, y);
    }

    private void drawHistogram(GuiGraphics graphics, int x, int y) {
        int[] buckets = SpacingTracker.getBuckets();
        int peak = 1;
        for (int c : buckets) peak = Math.max(peak, c);

        graphics.drawString(this.font, "Hit distances", x, y, 0xFFFFFF, false);
        y += 12;

        int rowH = 7;
        int maxBarW = 90;
        double reach = 3.0;

        for (int i = 0; i < buckets.length; i++) {
            double low = i * SpacingTracker.BUCKET_SIZE;
            if (low > reach + SpacingTracker.BUCKET_SIZE) break;

            int barW = (int) Math.round((double) buckets[i] / peak * maxBarW);
            int color = SpacingData.zoneOf(low + SpacingTracker.BUCKET_SIZE / 2, reach).argb;

            String tag = String.format("%.2f", low);
            graphics.drawString(this.font, tag, x, y, 0xFF777777, false);

            int barX = x + 26;
            graphics.fill(barX, y, barX + maxBarW, y + rowH - 2, 0x40000000);
            if (barW > 0) graphics.fill(barX, y, barX + barW, y + rowH - 2, color);
            if (buckets[i] > 0) {
                graphics.drawString(this.font, String.valueOf(buckets[i]),
                    barX + maxBarW + 4, y, 0xFFAAAAAA, false);
            }
            y += rowH;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - SpacingData.MIN_SCALE)
                 / (SpacingData.MAX_SCALE - SpacingData.MIN_SCALE);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (SpacingData.MAX_SCALE - SpacingData.MIN_SCALE) + SpacingData.MIN_SCALE);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("HUD size: " + denormalize()));
        }

        @Override
        protected void applyValue() {
            SpacingData.setScale(denormalize());
        }
    }
}
