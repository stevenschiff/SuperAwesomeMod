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
        ).bounds(cx - btnW / 2, cy - 68, btnW, btnH).build());

        // Icon size slider (0.5 – 5.0 in 0.1 steps)
        this.addRenderableWidget(new ScaleSlider(cx - btnW / 2, cy - 43, btnW, btnH,
                ArmorHudData.getScale()));

        // Text size slider (0.5 – 3.0 in 0.1 steps)
        this.addRenderableWidget(new TextScaleSlider(cx - btnW / 2, cy - 18, btnW, btnH,
                ArmorHudData.getTextScale()));

        // Durability height offset slider (0 – 30)
        this.addRenderableWidget(new HeightSlider(cx - btnW / 2, cy + 7, btnW, btnH,
                ArmorHudData.getDurabilityHeight()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 37, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(ArmorHudData.isEnabled() ? "Armor HUD: Enabled" : "Armor HUD: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 85, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Shows armor with exact durability numbers"),
            cx, cy - 71, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---- Size slider: 0.5 to 5.0 in 0.1 increments ----
    private static final class ScaleSlider extends AbstractSliderButton {
        private static final float MIN = 0.5f;
        private static final float MAX = 5.0f;

        ScaleSlider(int x, int y, int w, int h, float initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(float v) {
            return (v - MIN) / (MAX - MIN);
        }

        private float denormalize() {
            float raw = (float) (this.value * (MAX - MIN) + MIN);
            return Math.round(raw * 10.0f) / 10.0f; // snap to 0.1
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Size: " + String.format("%.1f", denormalize())));
        }

        @Override
        protected void applyValue() {
            ArmorHudData.setScale(denormalize());
        }
    }

    // ---- Text size slider: 0.5 to 3.0 in 0.1 increments ----
    private static final class TextScaleSlider extends AbstractSliderButton {
        private static final float MIN = 0.5f;
        private static final float MAX = 3.0f;

        TextScaleSlider(int x, int y, int w, int h, float initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(float v) {
            return (v - MIN) / (MAX - MIN);
        }

        private float denormalize() {
            float raw = (float) (this.value * (MAX - MIN) + MIN);
            return Math.round(raw * 10.0f) / 10.0f;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Text Size: " + String.format("%.1f", denormalize())));
        }

        @Override
        protected void applyValue() {
            ArmorHudData.setTextScale(denormalize());
        }
    }

    // ---- Durability height slider: 0 to 30 pixels ----
    private static final class HeightSlider extends AbstractSliderButton {
        private static final int MIN = 0;
        private static final int MAX = 30;

        HeightSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), (double) (initial - MIN) / (MAX - MIN));
            this.updateMessage();
        }

        private int denormalize() {
            return (int) Math.round(this.value * (MAX - MIN) + MIN);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Durability Height: " + denormalize()));
        }

        @Override
        protected void applyValue() {
            ArmorHudData.setDurabilityHeight(denormalize());
        }
    }
}
