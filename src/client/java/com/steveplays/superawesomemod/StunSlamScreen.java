package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StunSlamScreen extends Screen {

    private final Screen parent;

    public StunSlamScreen(Screen parent) {
        super(Component.literal("Stun Slam"));
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
                StunSlamData.setEnabled(!StunSlamData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 52, btnW, btnH).build());

        // Mode
        this.addRenderableWidget(Button.builder(
            modeLabel(),
            btn -> {
                StunSlamData.cycleMode();
                btn.setMessage(modeLabel());
            }
        ).bounds(cx - btnW / 2, cy - 28, btnW, btnH).build());

        // Click counts
        this.addRenderableWidget(new ClickSlider(cx - btnW / 2, cy - 4, btnW, btnH,
                "Axe clicks", StunSlamData.getAxeClicks(), StunSlamData::setAxeClicks));
        this.addRenderableWidget(new ClickSlider(cx - btnW / 2, cy + 20, btnW, btnH,
                "Mace clicks", StunSlamData.getMaceClicks(), StunSlamData::setMaceClicks));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 46, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(StunSlamData.isEnabled() ? "Disable Stun Slam" : "Enable Stun Slam");
    }

    private Component modeLabel() {
        return Component.literal("Mode: " + StunSlamData.getModeName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = StunSlamData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        String hint = StunSlamData.getMode() == StunSlamData.Mode.SWAP_ASSIST
            ? "Clicks fire on spear -> axe and axe -> mace only"
            : "Press the Stun Slam keybind to run the whole combo";
        graphics.drawCenteredString(this.font, Component.literal(hint), cx, cy + 72, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Clicks are 1 tick apart - the 2nd mace hit is stun, not damage"),
            cx, cy + 84, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ClickSlider extends AbstractSliderButton {
        private final String label;
        private final java.util.function.IntConsumer sink;

        ClickSlider(int x, int y, int w, int h, String label, int initial,
                    java.util.function.IntConsumer sink) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.label = label;
            this.sink = sink;
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - StunSlamData.MIN_CLICKS)
                 / (StunSlamData.MAX_CLICKS - StunSlamData.MIN_CLICKS);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (StunSlamData.MAX_CLICKS - StunSlamData.MIN_CLICKS) + StunSlamData.MIN_CLICKS);
        }

        @Override
        protected void updateMessage() {
            // label is null on the super() call that runs before the field is assigned
            if (this.label == null) return;
            this.setMessage(Component.literal(this.label + ": " + denormalize()));
        }

        @Override
        protected void applyValue() {
            if (this.sink == null) return;
            this.sink.accept(denormalize());
        }
    }
}
