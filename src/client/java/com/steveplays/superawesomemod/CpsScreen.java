package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CpsScreen extends Screen {

    private final Screen parent;

    public CpsScreen(Screen parent) {
        super(Component.literal("CPS Counter"));
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
                CpsData.setEnabled(!CpsData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 46, btnW, btnH).build());

        this.addRenderableWidget(new ScaleSlider(
            cx - btnW / 2, cy - 20, btnW, btnH,
            CpsData.getScale()
        ));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 14, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(CpsData.isEnabled()
            ? "CPS Counter: Enabled" : "CPS Counter: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Shows left & right click CPS next to the hotbar"),
            cx, cy - 58, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int w, int h, int initialScale) {
            super(x, y, w, h, Component.empty(), normalize(initialScale));
            this.updateMessage();
        }

        private static double normalize(int scale) {
            return (double) (scale - 1) / 9.0;
        }

        @Override
        protected void updateMessage() {
            int s = (int) Math.round(this.value * 9.0 + 1.0);
            this.setMessage(Component.literal("Size: " + s));
        }

        @Override
        protected void applyValue() {
            int s = (int) Math.round(this.value * 9.0 + 1.0);
            CpsData.setScale(s);
        }
    }
}
