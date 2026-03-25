package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.network.JumpHeightPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperAwesomeMod implements ModInitializer {

    public static final String MOD_ID = "superawesomemod";

    // SLF4J logger — use this everywhere instead of System.out.println
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[SuperAwesomeMod] Initializing — MC 1.21.11 / Fabric Loader");

        // Register the client→server jump height packet.
        // This fires regardless of cheat/operator status.
        PayloadTypeRegistry.playC2S().register(JumpHeightPayload.TYPE, JumpHeightPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(JumpHeightPayload.TYPE, (payload, context) -> {
            float value = Math.clamp(payload.multiplier(), PlayerJumpData.MIN, PlayerJumpData.MAX);
            PlayerJumpData.setMultiplier(context.player().getUUID(), value);
            context.player().sendSystemMessage(Component.literal(
                "[SuperAwesomeMod] Jump height set to " + value + "x"
            ));
        });

        ModEvents.register();
        ModCommands.register();
    }
}
