package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MotionBlurScreen extends Screen {

    private final Screen parent;

    public MotionBlurScreen(Screen parent) {
        super(Component.literal("Motion Blur"));
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
                MotionBlurData.setEnabled(!MotionBlurData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Strength slider 1..10
        this.addRenderableWidget(new StrengthSlider(
            cx - btnW / 2, cy - 14, btnW, btnH,
            MotionBlurData.getStrength()
        ));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(MotionBlurData.isEnabled()
            ? "Motion Blur: Enabled"
            : "Motion Blur: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 60, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class StrengthSlider extends AbstractSliderButton {
        StrengthSlider(int x, int y, int w, int h, int initialStrength) {
            super(x, y, w, h, Component.empty(),
                normalize(initialStrength));
            this.updateMessage();
        }

        private static double normalize(int strength) {
            return (double) (strength - MotionBlurData.MIN_STRENGTH)
                 / (double) (MotionBlurData.MAX_STRENGTH - MotionBlurData.MIN_STRENGTH);
        }

        @Override
        protected void updateMessage() {
            int str = (int) Math.round(this.value
                * (MotionBlurData.MAX_STRENGTH - MotionBlurData.MIN_STRENGTH)
                + MotionBlurData.MIN_STRENGTH);
            this.setMessage(Component.literal("Strength: " + str));
        }

        @Override
        protected void applyValue() {
            int str = (int) Math.round(this.value
                * (MotionBlurData.MAX_STRENGTH - MotionBlurData.MIN_STRENGTH)
                + MotionBlurData.MIN_STRENGTH);
            MotionBlurData.setStrength(str);
        }
    }
}
