package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AntiHungerScreen extends Screen {

    private final Screen parent;

    public AntiHungerScreen(Screen parent) {
        super(Component.literal("Anti-Hunger"));
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
                AntiHungerData.setEnabled(!AntiHungerData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 64, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Walk Report", AntiHungerData.isWalkReport()),
            btn -> {
                AntiHungerData.setWalkReport(!AntiHungerData.isWalkReport());
                btn.setMessage(sub("Walk Report", AntiHungerData.isWalkReport()));
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Freeze Hunger", AntiHungerData.isFreezeHunger()),
            btn -> {
                AntiHungerData.setFreezeHunger(!AntiHungerData.isFreezeHunger());
                btn.setMessage(sub("Freeze Hunger", AntiHungerData.isFreezeHunger()));
            }
        ).bounds(cx - btnW / 2, cy - 16, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            sub("Pause near players", AntiHungerData.isPauseNearPlayers()),
            btn -> {
                AntiHungerData.setPauseNearPlayers(!AntiHungerData.isPauseNearPlayers());
                btn.setMessage(sub("Pause near players", AntiHungerData.isPauseNearPlayers()));
            }
        ).bounds(cx - btnW / 2, cy + 8, btnW, btnH).build());

        this.addRenderableWidget(new RadiusSlider(cx - btnW / 2, cy + 32, btnW, btnH,
                AntiHungerData.getPauseRadius()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 56, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(AntiHungerData.isEnabled() ? "Disable Anti-Hunger" : "Enable Anti-Hunger");
    }

    private static Component sub(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 94, 0xFFFFFF);

        boolean on = AntiHungerData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 82, on ? 0x55FF55 : 0xFF5555);

        boolean host = this.minecraft.hasSingleplayerServer();

        graphics.drawCenteredString(this.font,
            Component.literal("Walk Report: sprint costs no hunger - works on any server"),
            cx, cy + 82, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal(host
                ? "Freeze Hunger: active - stops all hunger including attacking"
                : "Freeze Hunger: not active here - worlds you host only"),
            cx, cy + 94, host ? 0xAAAAAA : 0xFFAA00);

        graphics.drawCenteredString(this.font,
            Component.literal("Sprint speed while reporting a walk is what anticheat looks for"),
            cx, cy + 106, 0xFF5555);

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
            return (double) (v - AntiHungerData.MIN_PAUSE_RADIUS)
                 / (AntiHungerData.MAX_PAUSE_RADIUS - AntiHungerData.MIN_PAUSE_RADIUS);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (AntiHungerData.MAX_PAUSE_RADIUS - AntiHungerData.MIN_PAUSE_RADIUS)
                + AntiHungerData.MIN_PAUSE_RADIUS);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Pause radius: " + denormalize() + " blocks"));
        }

        @Override
        protected void applyValue() {
            AntiHungerData.setPauseRadius(denormalize());
        }
    }
}
