package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BodyTwistScreen extends Screen {

    private final Screen parent;

    public BodyTwistScreen(Screen parent) {
        super(Component.literal("Body Twist"));
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
                BodyTwistData.setEnabled(!BodyTwistData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new TwistSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                BodyTwistData.getTwistLimit()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(BodyTwistData.isEnabled() ? "Disable Body Twist" : "Enable Body Twist");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = BodyTwistData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Legs follow movement, torso follows aim"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Applies to every player you see  |  vanilla limit is 50"),
            cx, cy + 58, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Visual only - hitboxes and crosshair are unchanged"),
            cx, cy + 70, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class TwistSlider extends AbstractSliderButton {
        TwistSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - BodyTwistData.MIN_LIMIT)
                 / (BodyTwistData.MAX_LIMIT - BodyTwistData.MIN_LIMIT);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (BodyTwistData.MAX_LIMIT - BodyTwistData.MIN_LIMIT) + BodyTwistData.MIN_LIMIT);
        }

        @Override
        protected void updateMessage() {
            int v = denormalize();
            String suffix = v >= BodyTwistData.MAX_LIMIT ? " (unlimited)"
                          : v <= BodyTwistData.VANILLA_LIMIT ? " (vanilla)" : "";
            this.setMessage(Component.literal("Twist limit: " + v + suffix));
        }

        @Override
        protected void applyValue() {
            BodyTwistData.setTwistLimit(denormalize());
        }
    }
}
