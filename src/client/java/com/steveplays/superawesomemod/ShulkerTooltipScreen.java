package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ShulkerTooltipScreen extends Screen {

    private final Screen parent;

    public ShulkerTooltipScreen(Screen parent) {
        super(Component.literal("Shulker Tooltips"));
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
                ShulkerTooltipData.setEnabled(!ShulkerTooltipData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Scale slider 1..10
        this.addRenderableWidget(new ScaleSlider(
            cx - btnW / 2, cy - 14, btnW, btnH,
            ShulkerTooltipData.getScale()
        ));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 16, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(ShulkerTooltipData.isEnabled()
            ? "Shulker Tooltips: Enabled"
            : "Shulker Tooltips: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 68, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Hold Shift over a shulker box to preview contents"),
            cx, cy - 56, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int w, int h, int initialScale) {
            super(x, y, w, h, Component.empty(),
                normalize(initialScale));
            this.updateMessage();
        }

        private static double normalize(int scale) {
            return (double) (scale - ShulkerTooltipData.MIN_SCALE)
                 / (double) (ShulkerTooltipData.MAX_SCALE - ShulkerTooltipData.MIN_SCALE);
        }

        @Override
        protected void updateMessage() {
            int scale = (int) Math.round(this.value
                * (ShulkerTooltipData.MAX_SCALE - ShulkerTooltipData.MIN_SCALE)
                + ShulkerTooltipData.MIN_SCALE);
            this.setMessage(Component.literal("Scale: " + scale));
        }

        @Override
        protected void applyValue() {
            int scale = (int) Math.round(this.value
                * (ShulkerTooltipData.MAX_SCALE - ShulkerTooltipData.MIN_SCALE)
                + ShulkerTooltipData.MIN_SCALE);
            ShulkerTooltipData.setScale(scale);
        }
    }
}
