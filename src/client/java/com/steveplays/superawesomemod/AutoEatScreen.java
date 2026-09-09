package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoEatScreen extends Screen {

    private final Screen parent;

    public AutoEatScreen(Screen parent) {
        super(Component.literal("Auto Eat"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                AutoEatData.setEnabled(!AutoEatData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new ThresholdSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                AutoEatData.getThreshold()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(AutoEatData.isEnabled() ? "Disable Auto Eat" : "Enable Auto Eat");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = AutoEatData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Eats your best hotbar food, then restores your slot"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Waits if you're already using an item  |  any server"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ThresholdSlider extends AbstractSliderButton {
        ThresholdSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - AutoEatData.MIN_THRESHOLD)
                 / (AutoEatData.MAX_THRESHOLD - AutoEatData.MIN_THRESHOLD);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (AutoEatData.MAX_THRESHOLD - AutoEatData.MIN_THRESHOLD) + AutoEatData.MIN_THRESHOLD);
        }

        @Override
        protected void updateMessage() {
            int t = denormalize();
            this.setMessage(Component.literal(
                "Eat at or below: " + t + " (" + String.format("%.1f", t / 2.0) + " drumsticks)"));
        }

        @Override
        protected void applyValue() {
            AutoEatData.setThreshold(denormalize());
        }
    }
}
