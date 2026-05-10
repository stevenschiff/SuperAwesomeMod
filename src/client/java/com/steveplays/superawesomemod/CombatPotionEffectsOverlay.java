package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class CombatPotionEffectsOverlay {

    private CombatPotionEffectsOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(CombatPotionEffectsOverlay::onHudRender);
    }

    private static void onHudRender(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!CombatPotionEffectsData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) return;

        // Sort effects the same way vanilla does: beneficial first, then by duration descending.
        List<MobEffectInstance> sorted = new ArrayList<>(effects);
        sorted.sort(Comparator.comparingInt(MobEffectInstance::getDuration));

        int screenWidth = graphics.guiWidth();

        // Vanilla renders effect icons starting at x = screenWidth - 25 for the first icon,
        // then moving left by 25 for each subsequent icon. Icons are at y = 1.
        // The icon is 24x24 pixels. We render the timer text centered below each icon.
        int iconSize = 25;
        int iconY = 1;
        int textY = iconY + 26; // Just below the 24px icon

        for (int i = 0; i < sorted.size(); i++) {
            MobEffectInstance effect = sorted.get(i);
            int duration = effect.getDuration();
            if (duration <= 0) continue;

            String timeStr = formatDuration(duration);
            int iconX = screenWidth - iconSize * (i + 1);
            int textWidth = mc.font.width(timeStr);
            int textX = iconX + (iconSize - textWidth) / 2;

            // Draw with shadow for readability
            graphics.drawString(mc.font, timeStr, textX, textY, 0xFFFFFF, true);
        }
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("0:%02d", seconds);
    }
}
