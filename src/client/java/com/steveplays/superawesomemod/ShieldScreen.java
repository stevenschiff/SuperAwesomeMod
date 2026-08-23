package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ShieldScreen extends Screen {

    private final Screen parent;

    public ShieldScreen(Screen parent) {
        super(Component.literal("Shield"));
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
                ShieldData.setEnabled(!ShieldData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 52, btnW, btnH).build());

        this.addRenderableWidget(new ShieldSlider(cx - btnW / 2, cy - 28, btnW, btnH,
            "Shield transparency", ShieldData.MIN_TRANSPARENCY, ShieldData.MAX_TRANSPARENCY,
            ShieldData::getTransparency, ShieldData::setTransparency, true));

        this.addRenderableWidget(new ShieldSlider(cx - btnW / 2, cy - 4, btnW, btnH,
            "When used", ShieldData.MIN_OFFSET, ShieldData.MAX_OFFSET,
            ShieldData::getUsedOffset, ShieldData::setUsedOffset, false));

        this.addRenderableWidget(new ShieldSlider(cx - btnW / 2, cy + 20, btnW, btnH,
            "When not used", ShieldData.MIN_OFFSET, ShieldData.MAX_OFFSET,
            ShieldData::getNotUsedOffset, ShieldData::setNotUsedOffset, false));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 44, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(ShieldData.isEnabled() ? "Disable Shield" : "Enable Shield");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = ShieldData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Offsets move the shield up or down on screen"),
            cx, cy + 70, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Used = holding right-click  |  Not used = just held"),
            cx, cy + 82, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Banner patterns stay solid; plain shields fade fully"),
            cx, cy + 94, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ShieldSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final IntConsumer sink;
        private final boolean percent;

        ShieldSlider(int x, int y, int w, int h, String label, int min, int max,
                     IntSupplier source, IntConsumer sink, boolean percent) {
            super(x, y, w, h, Component.empty(), (double) (source.getAsInt() - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.sink = sink;
            this.percent = percent;
            this.updateMessage();
        }

        private int denormalize() {
            return (int) Math.round(this.value * (this.max - this.min) + this.min);
        }

        @Override
        protected void updateMessage() {
            // Runs once from super() before the fields are assigned.
            if (this.label == null) return;
            int v = denormalize();
            this.setMessage(Component.literal(
                this.label + ": " + (this.percent ? v + "%" : (v > 0 ? "+" + v : String.valueOf(v)))));
        }

        @Override
        protected void applyValue() {
            if (this.sink == null) return;
            this.sink.accept(denormalize());
        }
    }
}
