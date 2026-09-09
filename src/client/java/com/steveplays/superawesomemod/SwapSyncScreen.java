package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SwapSyncScreen extends Screen {

    private final Screen parent;

    public SwapSyncScreen(Screen parent) {
        super(Component.literal("Swap Sync"));
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
                SwapSyncData.setEnabled(!SwapSyncData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        this.addRenderableWidget(new GuardSlider(cx - btnW / 2, cy - 10, btnW, btnH,
                SwapSyncData.getGuardTicks()));

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(SwapSyncData.isEnabled()
            ? "Disable Swap Sync" : "Enable Swap Sync");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean on = SwapSyncData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 58, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Drops swings sent too soon after a spear/axe/mace swap"),
            cx, cy + 46, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Higher = safer on a laggy server, but slower combos"),
            cx, cy + 58, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class GuardSlider extends AbstractSliderButton {
        GuardSlider(int x, int y, int w, int h, int initial) {
            super(x, y, w, h, Component.empty(), normalize(initial));
            this.updateMessage();
        }

        private static double normalize(int v) {
            return (double) (v - SwapSyncData.MIN_GUARD_TICKS)
                 / (SwapSyncData.MAX_GUARD_TICKS - SwapSyncData.MIN_GUARD_TICKS);
        }

        private int denormalize() {
            return (int) Math.round(this.value
                * (SwapSyncData.MAX_GUARD_TICKS - SwapSyncData.MIN_GUARD_TICKS)
                + SwapSyncData.MIN_GUARD_TICKS);
        }

        @Override
        protected void updateMessage() {
            int t = denormalize();
            this.setMessage(Component.literal(
                "Wait after swap: " + t + (t == 1 ? " tick" : " ticks")));
        }

        @Override
        protected void applyValue() {
            SwapSyncData.setGuardTicks(denormalize());
        }
    }
}
