package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.network.FlySpeedPayload;
import com.steveplays.superawesomemod.network.ToggleFlyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FlyScreen extends Screen {

    private final Screen parent;

    // Speed preset labels and their values (must stay in sync)
    private static final String[] SPEED_LABELS = { "Slow", "Normal", "Fast", "Very Fast" };
    private static final float[]  SPEED_VALUES  = {
        FlySpeedPayload.SLOW,
        FlySpeedPayload.NORMAL,
        FlySpeedPayload.FAST,
        FlySpeedPayload.VERY_FAST
    };

    public FlyScreen(Screen parent) {
        super(Component.literal("Flight"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width  / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        // Toggle button — label reflects current state from shared PlayerFlyData
        boolean canFly = isFlightEnabled();
        this.addRenderableWidget(Button.builder(
            Component.literal(canFly ? "Disable Flight" : "Enable Flight"),
            btn -> toggleFlight()
        ).bounds(cx - btnW / 2, cy - 40, btnW, btnH).build());

        // Speed preset buttons — evenly spaced in a single row
        int totalSpeedW = SPEED_LABELS.length * 48 + (SPEED_LABELS.length - 1) * 4;
        int speedStartX = cx - totalSpeedW / 2;
        for (int i = 0; i < SPEED_LABELS.length; i++) {
            final float speed = SPEED_VALUES[i];
            this.addRenderableWidget(Button.builder(
                Component.literal(SPEED_LABELS[i]),
                btn -> setSpeed(speed)
            ).bounds(speedStartX + i * 52, cy - 10, 48, btnH).build());
        }

        // Back
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 20, 100, btnH).build());
    }

    private void toggleFlight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean newState = !PlayerFlyData.isEnabled(mc.player.getUUID());

        // Write to the shared ConcurrentHashMap — the client tick hook reads this
        // every tick to maintain mayfly=true locally (works on any server).
        PlayerFlyData.setEnabled(mc.player.getUUID(), newState);

        // Apply immediately to this client so flight starts/stops without waiting
        // for the next tick. This works on any server — no packet round-trip needed.
        mc.player.getAbilities().mayfly = newState;
        mc.player.getAbilities().flying = newState;

        // Also send the packet so servers that have the mod installed get updated
        // server-side as well (for correct physics validation on those servers).
        ClientPlayNetworking.send(new ToggleFlyPayload(newState));

        this.minecraft.setScreen(this.parent);
    }

    private void setSpeed(float speed) {
        // Apply immediately client-side for zero-latency feel
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getAbilities().setFlyingSpeed(speed);
        }
        ClientPlayNetworking.send(new FlySpeedPayload(speed));
    }

    /**
     * Reads from PlayerFlyData (the shared ConcurrentHashMap) rather than the
     * client-side mayfly flag. This gives accurate state even before the server
     * has had a chance to sync the abilities packet back.
     */
    private boolean isFlightEnabled() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && PlayerFlyData.isEnabled(mc.player.getUUID());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);

        boolean canFly = isFlightEnabled();
        graphics.drawCenteredString(this.font,
            Component.literal("Status: " + (canFly ? "Enabled" : "Disabled")),
            cx, cy - 58, canFly ? 0x55FF55 : 0xFF5555);

        graphics.drawCenteredString(this.font,
            Component.literal("Speed:"),
            cx, cy - 24, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
