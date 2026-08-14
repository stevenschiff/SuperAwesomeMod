package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MaceTimingScreen extends Screen {

    private final Screen parent;

    public MaceTimingScreen(Screen parent) {
        super(Component.literal("Mace Timing"));
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
                MaceTimingData.setEnabled(!MaceTimingData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new StrengthSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                MaceTimingData.getMinStrengthPercent()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(MaceTimingData.isEnabled()
            ? "Disable Mace Timing" : "Enable Mace Timing");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = MaceTimingData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Drops mace clicks that land before the swing recharges"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Never swings for you - it stops one click poisoning the next"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class StrengthSlider extends AbstractSliderButton {
        StrengthSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - MaceTimingData.MIN_STRENGTH)
                 / (MaceTimingData.MAX_STRENGTH - MaceTimingData.MIN_STRENGTH);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (MaceTimingData.MAX_STRENGTH - MaceTimingData.MIN_STRENGTH)
                + MaceTimingData.MIN_STRENGTH);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Swing at: " + denormalize() + "% charge"));
        }

        @Override
        protected void applyValue() {
            MaceTimingData.setMinStrengthPercent(denormalize());
        }
    }
}
