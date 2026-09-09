package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AntiKnockbackScreen extends Screen {

    private final Screen parent;

    public AntiKnockbackScreen(Screen parent) {
        super(Component.literal("Anti-Knockback"));
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
                AntiKnockbackData.setEnabled(!AntiKnockbackData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Knockback taken (0-100%)
        this.addRenderableWidget(new KnockbackSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                AntiKnockbackData.getPercent()));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(AntiKnockbackData.isEnabled()
            ? "Disable Anti-Knockback"
            : "Enable Anti-Knockback");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = AntiKnockbackData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("0% = never pushed back  |  100% = normal"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Works on any server  |  covers hits and explosions"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class KnockbackSlider extends AbstractSliderButton {
        KnockbackSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - AntiKnockbackData.MIN_PERCENT)
                 / (AntiKnockbackData.MAX_PERCENT - AntiKnockbackData.MIN_PERCENT);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (AntiKnockbackData.MAX_PERCENT - AntiKnockbackData.MIN_PERCENT)
                + AntiKnockbackData.MIN_PERCENT);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Knockback taken: " + denormalize() + "%"));
        }

        @Override
        protected void applyValue() {
            AntiKnockbackData.setPercent(denormalize());
        }
    }
}
