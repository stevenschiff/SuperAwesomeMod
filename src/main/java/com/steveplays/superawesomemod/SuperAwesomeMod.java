package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.network.AttackRangePayload;
import com.steveplays.superawesomemod.network.FlySpeedPayload;
import com.steveplays.superawesomemod.network.JumpHeightPayload;
import com.steveplays.superawesomemod.network.ToggleFlyPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperAwesomeMod implements ModInitializer {

    public static final String MOD_ID = "superawesomemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[SuperAwesomeMod] Initializing — MC 1.21.11 / Fabric Loader");

        registerPackets();
        registerFlyReapply();
        ModEvents.register();
        ModCommands.register();
    }

    private static void registerPackets() {
        // --- Jump height ---
        PayloadTypeRegistry.playC2S().register(JumpHeightPayload.TYPE, JumpHeightPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(JumpHeightPayload.TYPE, (payload, context) -> {
            float value = Math.clamp(payload.multiplier(), PlayerJumpData.MIN, PlayerJumpData.MAX);
            PlayerJumpData.setMultiplier(context.player().getUUID(), value);
            context.player().sendSystemMessage(Component.literal(
                "[SuperAwesomeMod] Jump height set to " + value + "x"
            ));
        });

        // --- Flight toggle ---
        PayloadTypeRegistry.playC2S().register(ToggleFlyPayload.TYPE, ToggleFlyPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleFlyPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            boolean enable = !PlayerFlyData.isEnabled(player.getUUID());
            PlayerFlyData.setEnabled(player.getUUID(), enable);
            player.getAbilities().mayfly = enable;
            if (!enable) player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendSystemMessage(Component.literal(
                "[SuperAwesomeMod] Flight " + (enable ? "enabled" : "disabled")
            ));
        });

        // --- Fly speed ---
        PayloadTypeRegistry.playC2S().register(FlySpeedPayload.TYPE, FlySpeedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FlySpeedPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getAbilities().setFlyingSpeed(payload.speed());
            player.onUpdateAbilities();
        });

        // --- Attack range ---
        PayloadTypeRegistry.playC2S().register(AttackRangePayload.TYPE, AttackRangePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AttackRangePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            double range = Math.clamp(payload.range(), AttackRangePayload.MIN, AttackRangePayload.MAX);
            AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (attr != null) attr.setBaseValue(range);
            player.sendSystemMessage(Component.literal(
                "[SuperAwesomeMod] Attack range set to " + range + " blocks"
            ));
        });

        // Clean up fly state when a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PlayerFlyData.remove(handler.player.getUUID())
        );
    }

    /**
     * Every server tick, re-apply mayfly for players who have mod flight enabled.
     * This corrects any silent reset caused by vanilla respawn or game-mode code
     * running on survival players.
     */
    private static void registerFlyReapply() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (PlayerFlyData.isEnabled(player.getUUID()) && !player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            }
        });
    }
}
