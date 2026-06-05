package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FartherPlayersScreen extends Screen {

    private final Screen parent;

    public FartherPlayersScreen(Screen parent) {
        super(Component.literal("Farther Players"));
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
                FartherPlayersData.setEnabled(!FartherPlayersData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 46, btnW, btnH).build());

        this.addRenderableWidget(new DistanceSlider(
            cx - btnW / 2, cy - 20, btnW, btnH,
            FartherPlayersData.getDistance()
        ));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 14, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(FartherPlayersData.isEnabled()
            ? "Farther Players: Enabled" : "Farther Players: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("See player entities from farther away (64-512 blocks)"),
            cx, cy - 58, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        DistanceSlider(int x, int y, int w, int h, int initialDistance) {
            super(x, y, w, h, Component.empty(), normalize(initialDistance));
            this.updateMessage();
        }

        private static double normalize(int distance) {
            return (double) (distance - FartherPlayersData.MIN_DISTANCE)
                 / (double) (FartherPlayersData.MAX_DISTANCE - FartherPlayersData.MIN_DISTANCE);
        }

        @Override
        protected void updateMessage() {
            int dist = (int) Math.round(this.value
                * (FartherPlayersData.MAX_DISTANCE - FartherPlayersData.MIN_DISTANCE)
                + FartherPlayersData.MIN_DISTANCE);
            this.setMessage(Component.literal("Player Render Distance: " + dist + " blocks"));
        }

        @Override
        protected void applyValue() {
            int dist = (int) Math.round(this.value
                * (FartherPlayersData.MAX_DISTANCE - FartherPlayersData.MIN_DISTANCE)
                + FartherPlayersData.MIN_DISTANCE);
            FartherPlayersData.setDistance(dist);
        }
    }
}
