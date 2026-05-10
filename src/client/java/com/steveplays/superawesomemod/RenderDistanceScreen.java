package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RenderDistanceScreen extends Screen {

    private final Screen parent;

    public RenderDistanceScreen(Screen parent) {
        super(Component.literal("Render Distance"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        // Toggle on/off
        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                RenderDistanceData.setEnabled(!RenderDistanceData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Distance slider 32..128
        this.addRenderableWidget(new DistanceSlider(
            cx - btnW / 2, cy - 14, btnW, btnH,
            RenderDistanceData.getDistance()
        ));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 16, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(RenderDistanceData.isEnabled()
            ? "Render Distance: Enabled"
            : "Render Distance: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 68, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Extends chunk render distance (32-128 chunks)"),
            cx, cy - 56, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        DistanceSlider(int x, int y, int w, int h, int initialDistance) {
            super(x, y, w, h, Component.empty(),
                normalize(initialDistance));
            this.updateMessage();
        }

        private static double normalize(int distance) {
            return (double) (distance - RenderDistanceData.MIN_DISTANCE)
                 / (double) (RenderDistanceData.MAX_DISTANCE - RenderDistanceData.MIN_DISTANCE);
        }

        @Override
        protected void updateMessage() {
            int dist = (int) Math.round(this.value
                * (RenderDistanceData.MAX_DISTANCE - RenderDistanceData.MIN_DISTANCE)
                + RenderDistanceData.MIN_DISTANCE);
            this.setMessage(Component.literal("Distance: " + dist + " chunks"));
        }

        @Override
        protected void applyValue() {
            int dist = (int) Math.round(this.value
                * (RenderDistanceData.MAX_DISTANCE - RenderDistanceData.MIN_DISTANCE)
                + RenderDistanceData.MIN_DISTANCE);
            RenderDistanceData.setDistance(dist);
        }
    }
}
