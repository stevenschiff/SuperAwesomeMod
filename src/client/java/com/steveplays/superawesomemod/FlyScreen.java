package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.network.ToggleFlyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FlyScreen extends Screen {

    private final Screen parent;

    public FlyScreen(Screen parent) {
        super(Component.literal("Flight"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Toggle button label reflects current state
        boolean canFly = isFlightEnabled();
        this.addRenderableWidget(Button.builder(
            Component.literal(canFly ? "Disable Flight" : "Enable Flight"),
            btn -> {
                ClientPlayNetworking.send(new ToggleFlyPayload());
                this.minecraft.setScreen(this.parent);
            }
        ).bounds(cx - 100, cy - 10, 200, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 16, 100, 20).build());
    }

    private boolean isFlightEnabled() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getAbilities().mayfly;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 50, 0xFFFFFF);

        boolean canFly = isFlightEnabled();
        int statusColor = canFly ? 0x55FF55 : 0xFF5555;
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (canFly ? "Enabled" : "Disabled")),
            cx, cy - 30, statusColor);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
