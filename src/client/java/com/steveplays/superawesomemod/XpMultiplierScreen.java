package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class XpMultiplierScreen extends Screen {

    private final Screen parent;

    public XpMultiplierScreen(Screen parent) {
        super(Component.literal("XP Multiplier"));
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
                XpMultiplierData.setEnabled(!XpMultiplierData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Multiplier slider (0.1x - 100x)
        this.addRenderableWidget(new MultiplierSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                XpMultiplierData.getMultiplier()));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(XpMultiplierData.isEnabled()
            ? "Disable XP Multiplier"
            : "Enable XP Multiplier");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = XpMultiplierData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Multiplies XP orbs, furnace XP and mob kills"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Worlds you host only - servers award their own XP"),
            cx, cy + 58, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── Multiplier slider (0.1x - 100x) ─────────────────────────────────
    //
    // Mapped by powers of ten rather than evenly, so 0.1x / 1x / 10x / 100x sit at
    // even spacing along the track. A linear 0.1-100 slider would spend its first
    // pixel on everything below 1x and make 1x itself unpickable.

    private static final class MultiplierSlider extends AbstractSliderButton {
        private static final double LOG_MIN = Math.log10(XpMultiplierData.MIN_MULTIPLIER);
        private static final double LOG_MAX = Math.log10(XpMultiplierData.MAX_MULTIPLIER);

        MultiplierSlider(int x, int y, int w, int h, float initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(float v) {
            return (Math.log10(v) - LOG_MIN) / (LOG_MAX - LOG_MIN);
        }

        /** Snaps to 0.1 steps below 10x and whole numbers above, so the label stays readable. */
        private float denormalize() {
            float v = (float) Math.pow(10.0, LOG_MIN + this.value * (LOG_MAX - LOG_MIN));
            v = v < 10f ? Math.round(v * 10f) / 10f : Math.round(v);
            return Math.clamp(v, XpMultiplierData.MIN_MULTIPLIER, XpMultiplierData.MAX_MULTIPLIER);
        }

        @Override
        protected void updateMessage() {
            float v = denormalize();
            String text = v < 10f
                ? String.format("%.1f", v)
                : String.valueOf((int) v);
            this.setMessage(Component.literal("Multiplier: " + text + "x"));
        }

        @Override
        protected void applyValue() {
            XpMultiplierData.setMultiplier(denormalize());
        }
    }
}
