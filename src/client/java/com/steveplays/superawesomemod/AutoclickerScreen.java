package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoclickerScreen extends Screen {

    private final Screen parent;

    public AutoclickerScreen(Screen parent) {
        super(Component.literal("Autoclicker"));
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
                AutoclickerData.setEnabled(!AutoclickerData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 50, btnW, btnH).build());

        // Left / Right click selector — two side-by-side buttons
        int half = (btnW - 4) / 2;
        Button leftBtn = Button.builder(
            buttonLabel(AutoclickerData.Button.LEFT),
            btn -> {
                AutoclickerData.setButton(AutoclickerData.Button.LEFT);
                this.rebuildWidgets();
            }
        ).bounds(cx - btnW / 2, cy - 24, half, btnH).build();
        this.addRenderableWidget(leftBtn);

        Button rightBtn = Button.builder(
            buttonLabel(AutoclickerData.Button.RIGHT),
            btn -> {
                AutoclickerData.setButton(AutoclickerData.Button.RIGHT);
                this.rebuildWidgets();
            }
        ).bounds(cx - btnW / 2 + half + 4, cy - 24, half, btnH).build();
        this.addRenderableWidget(rightBtn);

        // CPS slider 1..100
        this.addRenderableWidget(new CpsSlider(cx - btnW / 2, cy + 2, btnW, btnH, AutoclickerData.getCps()));

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 36, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(AutoclickerData.isEnabled() ? "Autoclicker: Enabled" : "Autoclicker: Disabled");
    }

    private Component buttonLabel(AutoclickerData.Button b) {
        boolean selected = AutoclickerData.getButton() == b;
        String name = b == AutoclickerData.Button.LEFT ? "Left Click" : "Right Click";
        return Component.literal((selected ? "[ " : "  ") + name + (selected ? " ]" : "  "));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 78, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Only fires while no menu is open"),
            cx, cy - 66, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class CpsSlider extends AbstractSliderButton {
        CpsSlider(int x, int y, int w, int h, int initialCps) {
            super(x, y, w, h, Component.empty(),
                normalize(initialCps));
            this.updateMessage();
        }

        private static double normalize(int cps) {
            return (double) (cps - AutoclickerData.MIN_CPS)
                 / (double) (AutoclickerData.MAX_CPS - AutoclickerData.MIN_CPS);
        }

        @Override
        protected void updateMessage() {
            int cps = (int) Math.round(this.value
                * (AutoclickerData.MAX_CPS - AutoclickerData.MIN_CPS)
                + AutoclickerData.MIN_CPS);
            this.setMessage(Component.literal("CPS: " + cps));
        }

        @Override
        protected void applyValue() {
            int cps = (int) Math.round(this.value
                * (AutoclickerData.MAX_CPS - AutoclickerData.MIN_CPS)
                + AutoclickerData.MIN_CPS);
            AutoclickerData.setCps(cps);
        }
    }
}
