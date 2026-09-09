package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BHopScreen extends Screen {

    private final Screen parent;

    public BHopScreen(Screen parent) {
        super(Component.literal("B Hop"));
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
                BHopData.setEnabled(!BHopData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new SpeedSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                BHopData.getBlocksPerSecond()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(BHopData.isEnabled() ? "Disable B Hop" : "Enable B Hop");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = BHopData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Hold a movement key  |  hops and steers automatically"),
            cx, cy + 46, 0xAAAAAA);

        int speed = BHopData.getBlocksPerSecond();
        if (speed > BHopData.SERVER_SAFE_LIMIT) {
            graphics.drawCenteredString(this.font,
                Component.literal("Over " + BHopData.SERVER_SAFE_LIMIT + " a server teleports you back"),
                cx, cy + 58, 0xFF5555);
        } else {
            graphics.drawCenteredString(this.font,
                Component.literal("Vanilla sprint is about 5.6  |  anticheat notices well below 200"),
                cx, cy + 58, 0xFFAA00);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class SpeedSlider extends AbstractSliderButton {
        SpeedSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - BHopData.MIN_BLOCKS_PER_SECOND)
                 / (BHopData.MAX_BLOCKS_PER_SECOND - BHopData.MIN_BLOCKS_PER_SECOND);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (BHopData.MAX_BLOCKS_PER_SECOND - BHopData.MIN_BLOCKS_PER_SECOND)
                + BHopData.MIN_BLOCKS_PER_SECOND);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Speed: " + denormalize() + " blocks/sec"));
        }

        @Override
        protected void applyValue() {
            BHopData.setBlocksPerSecond(denormalize());
        }
    }
}
