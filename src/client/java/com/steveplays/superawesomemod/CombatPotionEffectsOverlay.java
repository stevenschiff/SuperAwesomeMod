package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class CombatPotionEffectsOverlay {

    private CombatPotionEffectsOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(CombatPotionEffectsOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        try {
            if (!CombatPotionEffectsData.isEnabled()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return;

            LocalPlayer player = mc.player;
            if (player == null) return;

            List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
            if (effects.isEmpty()) return;

            int screenWidth = graphics.guiWidth();
            int lineHeight = 11;
            // Render below vanilla's effect icon area.
            int startY = 52;
            int padding = 4;
            int rendered = 0;

            for (MobEffectInstance effect : effects) {
                int duration = effect.getDuration();
                // Show all effects that have a positive, finite duration.
                if (duration <= 0) continue;

                String name;
                try {
                    name = effect.getEffect().value().getDisplayName().getString();
                } catch (Exception e) {
                    name = "Effect";
                }

                int amplifier = effect.getAmplifier();
                if (amplifier > 0) {
                    name += " " + toRoman(amplifier + 1);
                }

                String timeStr = formatDuration(duration);
                String line = name + " " + timeStr;

                int textWidth = mc.font.width(line);
                int x = screenWidth - textWidth - padding;
                int y = startY + rendered * lineHeight;

                // Color based on remaining time: white normally, yellow < 30s, red < 10s.
                int color = 0xFFFFFF;
                int seconds = duration / 20;
                if (seconds < 10) {
                    color = 0xFF5555;
                } else if (seconds < 30) {
                    color = 0xFFFF55;
                }

                graphics.drawString(mc.font, line, x, y, color, true);
                rendered++;
            }
        } catch (Exception ignored) {
            // Prevent any exception from crashing the game.
        }
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}
