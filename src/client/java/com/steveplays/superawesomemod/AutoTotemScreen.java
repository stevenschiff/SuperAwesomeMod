package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoTotemScreen extends Screen {

    private final Screen parent;

    public AutoTotemScreen(Screen parent) {
        super(Component.literal("Auto Totem"));
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
                AutoTotemData.setEnabled(!AutoTotemData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new DelaySlider(cx - btnW / 2, cy - 10, btnW, btnH,
                AutoTotemData.getDelayTicks()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(AutoTotemData.isEnabled() ? "Disable Auto Totem" : "Enable Auto Totem");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = AutoTotemData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Refills the offhand the moment a totem pops"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Pauses while a container is open  |  any server"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class DelaySlider extends AbstractSliderButton {
        DelaySlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - AutoTotemData.MIN_DELAY)
                 / (AutoTotemData.MAX_DELAY - AutoTotemData.MIN_DELAY);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (AutoTotemData.MAX_DELAY - AutoTotemData.MIN_DELAY) + AutoTotemData.MIN_DELAY);
        }

        @Override
        protected void updateMessage() {
            int ticks = denormalize();
            this.setMessage(Component.literal(
                "Delay: " + ticks + (ticks == 1 ? " tick" : " ticks")));
        }

        @Override
        protected void applyValue() {
            AutoTotemData.setDelayTicks(denormalize());
        }
    }
}
