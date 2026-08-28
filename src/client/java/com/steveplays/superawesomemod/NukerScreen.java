package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NukerScreen extends Screen {

    private final Screen parent;

    public NukerScreen(Screen parent) {
        super(Component.literal("Nuker"));
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
                NukerData.setEnabled(!NukerData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 52, btnW, btnH).build());

        this.addRenderableWidget(new RadiusSlider(cx - btnW / 2, cy - 28, btnW, btnH,
                NukerData.getRadius()));

        this.addRenderableWidget(Button.builder(
            instantLabel(),
            btn -> {
                NukerData.setInstantOnly(!NukerData.isInstantOnly());
                btn.setMessage(instantLabel());
            }
        ).bounds(cx - btnW / 2, cy - 4, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(NukerData.isEnabled() ? "Disable Nuker" : "Enable Nuker");
    }

    private static Component instantLabel() {
        return Component.literal("Instant blocks only: " + (NukerData.isInstantOnly() ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = NukerData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Instant for grass, crops, torches, leaves w/ shears"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Stone and ore are never instant - the server times those"),
            cx, cy + 58, 0xFFAA00);

        graphics.drawCenteredString(this.font,
            Component.literal("With instant-only off, hard blocks go one at a time"),
            cx, cy + 70, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class RadiusSlider extends AbstractSliderButton {
        RadiusSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - NukerData.MIN_RADIUS) / (NukerData.MAX_RADIUS - NukerData.MIN_RADIUS);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (NukerData.MAX_RADIUS - NukerData.MIN_RADIUS) + NukerData.MIN_RADIUS);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Radius: " + denormalize() + " blocks"));
        }

        @Override
        protected void applyValue() {
            NukerData.setRadius(denormalize());
        }
    }
}
