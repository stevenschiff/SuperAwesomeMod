package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FastActionScreen extends Screen {

    private final Screen parent;

    public FastActionScreen(Screen parent) {
        super(Component.literal("Fast Actions"));
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
                FastActionData.setEnabled(!FastActionData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 52, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            subLabel("Fast Break", FastActionData.getFastBreakRaw()),
            btn -> {
                FastActionData.setFastBreak(!FastActionData.getFastBreakRaw());
                btn.setMessage(subLabel("Fast Break", FastActionData.getFastBreakRaw()));
            }
        ).bounds(cx - btnW / 2, cy - 28, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            subLabel("Fast Place", FastActionData.getFastPlaceRaw()),
            btn -> {
                FastActionData.setFastPlace(!FastActionData.getFastPlaceRaw());
                btn.setMessage(subLabel("Fast Place", FastActionData.getFastPlaceRaw()));
            }
        ).bounds(cx - btnW / 2, cy - 4, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            subLabel("Auto Tool", FastActionData.getAutoToolRaw()),
            btn -> {
                FastActionData.setAutoTool(!FastActionData.getAutoToolRaw());
                btn.setMessage(subLabel("Auto Tool", FastActionData.getAutoToolRaw()));
            }
        ).bounds(cx - btnW / 2, cy + 20, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 46, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(FastActionData.isEnabled()
            ? "Disable Fast Actions" : "Enable Fast Actions");
    }

    private static Component subLabel(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 82, 0xFFFFFF);

        boolean on = FastActionData.isEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (on ? "Enabled" : "Disabled")),
            cx, cy - 70, on ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Removes the delay between blocks, not the break itself"),
            cx, cy + 72, 0xAAAAAA);

        graphics.drawCenteredString(this.font,
            Component.literal("Fast Place = 20/sec while held, for any item you hold"),
            cx, cy + 84, 0xFFAA00);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
