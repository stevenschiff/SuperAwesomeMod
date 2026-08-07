package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.XpMultiplierData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * XP Multiplier: scales experience as it is awarded, so orbs, furnace output,
 * bottles o' enchanting and mob kills all pay out more.
 *
 * <p>Experience is server state — the client's level and progress bar are overwritten
 * by the next {@code ClientboundSetExperiencePacket}, so scaling anything on the
 * client would only desync the display. This hooks the award itself, which means it
 * takes effect in worlds this game hosts (singleplayer, and LAN worlds where you are
 * the host). A remote server awards its own XP and no client-side mod can change
 * that. The UUID check keeps it to your own XP rather than every player on your LAN
 * world.
 */
@Mixin(Player.class)
public abstract class XpMultiplierMixin {

    @ModifyVariable(method = "giveExperiencePoints", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int superawesomemod$multiplyXp(int points) {
        if (!XpMultiplierData.isEnabled()) return points;

        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return points;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getUUID().equals(self.getUUID())) return points;

        return XpMultiplierData.apply(points);
    }
}
