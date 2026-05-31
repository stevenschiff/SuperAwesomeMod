package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MinPingScreen extends Screen {

    private final Screen parent;

    public MinPingScreen(Screen parent) {
        super(Component.literal("Min Ping"));
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
                MinPingData.setEnabled(!MinPingData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Min ping slider 0..500
        this.addRenderableWidget(new PingSlider(
            cx - btnW / 2, cy - 14, btnW, btnH,
            MinPingData.getMinPingMs()
        ));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(MinPingData.isEnabled()
            ? "Min Ping: Enabled"
            : "Min Ping: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 60, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class PingSlider extends AbstractSliderButton {
        PingSlider(int x, int y, int w, int h, int initialPing) {
            super(x, y, w, h, Component.empty(),
                normalize(initialPing));
            this.updateMessage();
        }

        private static double normalize(int ping) {
            return (double) (ping - MinPingData.MIN_PING)
                 / (double) (MinPingData.MAX_PING - MinPingData.MIN_PING);
        }

        @Override
        protected void updateMessage() {
            int ms = (int) Math.round(this.value
                * (MinPingData.MAX_PING - MinPingData.MIN_PING)
                + MinPingData.MIN_PING);
            this.setMessage(Component.literal("Min Ping: " + ms + " ms"));
        }

        @Override
        protected void applyValue() {
            int ms = (int) Math.round(this.value
                * (MinPingData.MAX_PING - MinPingData.MIN_PING)
                + MinPingData.MIN_PING);
            MinPingData.setMinPingMs(ms);
        }
    }
}
