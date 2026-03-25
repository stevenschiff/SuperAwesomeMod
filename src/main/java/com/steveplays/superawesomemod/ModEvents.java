package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class ModEvents {

    public static void register() {
        // Fires on the server side whenever a living entity takes damage.
        // Return false to cancel the damage; return true to allow it.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (LivingEntity entity, DamageSource source, float amount) -> {

                if (entity instanceof ServerPlayer player) {
                    player.sendSystemMessage(
                        Component.literal("Ouch! You took " + amount + " damage from " + source.typeHolder().getRegisteredName() + "!")
                    );
                }

                return true; // allow the damage to proceed
            }
        );
    }
}
