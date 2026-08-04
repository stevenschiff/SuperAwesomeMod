package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BoatFlyScreen extends Screen {

    private final Screen parent;

    public BoatFlyScreen(Screen parent) {
        super(Component.literal("Boat Fly"));
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
                BoatFlyData.setEnabled(!BoatFlyData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Speed slider (1-250 blocks/second)
        this.addRenderableWidget(new SpeedSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                BoatFlyData.getBlocksPerSecond()));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(BoatFlyData.isEnabled() ? "Disable Boat Fly" : "Enable Boat Fly");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = BoatFlyData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Fly while riding a boat  |  Space/Shift for up/down"),
            cx, cy + 46, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── Speed slider (1-250 blocks/second) ──────────────────────────────

    private static final class SpeedSlider extends AbstractSliderButton {
        private static final int MIN = 1;
        private static final int MAX = 250;

        SpeedSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - MIN) / (MAX - MIN);
        }

        private int denormalize() {
            return (int) Math.round(this.value * (MAX - MIN) + MIN);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Speed: " + denormalize() + " blocks/sec"));
        }

        @Override
        protected void applyValue() {
            BoatFlyData.setBlocksPerSecond(denormalize());
        }
    }
}
