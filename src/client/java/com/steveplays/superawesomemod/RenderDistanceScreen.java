package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RenderDistanceScreen extends Screen {

    private final Screen parent;
    private EditBox seedBox;

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
        ).bounds(cx - btnW / 2, cy - 60, btnW, btnH).build());

        // Distance slider 32..128
        this.addRenderableWidget(new DistanceSlider(
            cx - btnW / 2, cy - 34, btnW, btnH,
            RenderDistanceData.getDistance()
        ));

        // Seed input
        seedBox = new EditBox(this.font, cx - btnW / 2, cy - 6, btnW, btnH, Component.literal("Seed"));
        seedBox.setHint(Component.literal("World seed (auto in singleplayer)"));
        if (RenderDistanceData.isSeedSet()) {
            seedBox.setValue(String.valueOf(RenderDistanceData.getSeed()));
        }
        this.addRenderableWidget(seedBox);

        // Apply seed button
        this.addRenderableWidget(Button.builder(
            Component.literal("Apply Seed"),
            btn -> {
                String text = seedBox.getValue().trim();
                if (!text.isEmpty()) {
                    try {
                        long seed = Long.parseLong(text);
                        RenderDistanceData.setSeed(seed);
                    } catch (NumberFormatException e) {
                        // Use string hash as seed (same as MC world creation).
                        RenderDistanceData.setSeed(text.hashCode());
                    }
                }
            }
        ).bounds(cx - btnW / 2, cy + 20, btnW, btnH).build());

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 48, 100, btnH).build());
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
        graphics.drawCenteredString(this.font, this.title, cx, cy - 86, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("LOD terrain + extended render (32-128 chunks)"),
            cx, cy - 74, 0xAAAAAA);
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
