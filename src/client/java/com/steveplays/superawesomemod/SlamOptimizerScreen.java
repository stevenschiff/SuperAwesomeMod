package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SlamOptimizerScreen extends Screen {

    private final Screen parent;

    public SlamOptimizerScreen(Screen parent) {
        super(Component.literal("Stun Slam Optimizer"));
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
                SlamOptimizerData.setEnabled(!SlamOptimizerData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new WaitSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                SlamOptimizerData.getMaxWaitTicks()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(SlamOptimizerData.isEnabled()
            ? "Disable Stun Slam Optimizer" : "Enable Stun Slam Optimizer");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = SlamOptimizerData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Only during spear to axe and axe to mace"),
            cx, cy + 40, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Settle scales with your ping  |  you do every click"),
            cx, cy + 52, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Outside a combo your clicks are never touched"),
            cx, cy + 64, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class WaitSlider extends AbstractSliderButton {
        WaitSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - SlamOptimizerData.MIN_MAX_WAIT)
                 / (SlamOptimizerData.MAX_MAX_WAIT - SlamOptimizerData.MIN_MAX_WAIT);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (SlamOptimizerData.MAX_MAX_WAIT - SlamOptimizerData.MIN_MAX_WAIT)
                + SlamOptimizerData.MIN_MAX_WAIT);
        }

        @Override
        protected void updateMessage() {
            int t = denormalize();
            this.setMessage(Component.literal(
                "Max settle: " + t + (t == 1 ? " tick" : " ticks")));
        }

        @Override
        protected void applyValue() {
            SlamOptimizerData.setMaxWaitTicks(denormalize());
        }
    }
}
