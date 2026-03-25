package com.steveplays.superawesomemod;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                Commands.literal("jumpheight")
                    // /jumpheight  — show current value
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                            float current = PlayerJumpData.getMultiplier(player.getUUID());
                            player.sendSystemMessage(Component.literal(
                                "[SuperAwesomeMod] Jump multiplier: " + current + "x  (range " +
                                PlayerJumpData.MIN + " – " + PlayerJumpData.MAX + ")"
                            ));
                        }
                        return 1;
                    })
                    // /jumpheight <value>  — set to value
                    .then(Commands.argument("multiplier",
                            FloatArgumentType.floatArg(PlayerJumpData.MIN, PlayerJumpData.MAX))
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                float value = FloatArgumentType.getFloat(ctx, "multiplier");
                                PlayerJumpData.setMultiplier(player.getUUID(), value);
                                player.sendSystemMessage(Component.literal(
                                    "[SuperAwesomeMod] Jump height set to " + value + "x"
                                ));
                            }
                            return 1;
                        })
                    )
            )
        );
    }
}
