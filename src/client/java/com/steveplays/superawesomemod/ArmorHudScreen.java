package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ArmorHudScreen extends Screen {

    private final Screen parent;

    public ArmorHudScreen(Screen parent) {
        super(Component.literal("Armor HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                ArmorHudData.setEnabled(!ArmorHudData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Size slider (1-5)
        this.addRenderableWidget(new ScaleSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                ArmorHudData.getScale()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(ArmorHudData.isEnabled() ? "Armor HUD: Enabled" : "Armor HUD: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Shows armor with exact durability numbers"),
            cx, cy - 56, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ScaleSlider extends AbstractSliderButton {
        private static final int MIN = 1;
        private static final int MAX = 5;

        ScaleSlider(int x, int y, int w, int h, int initial) {
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
            this.setMessage(Component.literal("Size: " + denormalize()));
        }

        @Override
        protected void applyValue() {
            ArmorHudData.setScale(denormalize());
        }
    }
}
