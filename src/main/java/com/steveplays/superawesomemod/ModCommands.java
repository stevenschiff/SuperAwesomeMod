package com.steveplays.superawesomemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // --- /npc ---
            dispatcher.register(
                Commands.literal("npc")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                    // /npc summon <name>
                    .then(Commands.literal("summon")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ModCommands::npcSummon)))

                    // /npc remove <name>
                    .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                FakePlayerManager.getNames(), builder))
                            .executes(ModCommands::npcRemove)))

                    // /npc removeall
                    .then(Commands.literal("removeall")
                        .executes(ModCommands::npcRemoveAll))

                    // /npc list
                    .then(Commands.literal("list")
                        .executes(ModCommands::npcList))

                    // /npc give <name> mainhand|offhand <item>
                    .then(Commands.literal("give")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                FakePlayerManager.getNames(), builder))
                            .then(Commands.literal("mainhand")
                                .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                    .executes(ctx -> npcGive(ctx, InteractionHand.MAIN_HAND))))
                            .then(Commands.literal("offhand")
                                .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                    .executes(ctx -> npcGive(ctx, InteractionHand.OFF_HAND))))))

                    // /npc use <name> mainhand|offhand
                    .then(Commands.literal("use")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                FakePlayerManager.getNames(), builder))
                            .then(Commands.literal("mainhand")
                                .executes(ctx -> npcUse(ctx, InteractionHand.MAIN_HAND)))
                            .then(Commands.literal("offhand")
                                .executes(ctx -> npcUse(ctx, InteractionHand.OFF_HAND)))))

                    // /npc shielddelay <name> [ticks]
                    .then(Commands.literal("shielddelay")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                FakePlayerManager.getNames(), builder))
                            .executes(ModCommands::npcShieldDelayGet)
                            .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                .executes(ModCommands::npcShieldDelaySet))))
            );
        });
    }

    // ------------------------------------------------------------------
    // NPC command handlers
    // ------------------------------------------------------------------

    private static int npcSummon(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        CommandSourceStack source = ctx.getSource();

        if (FakePlayerManager.exists(name)) {
            source.sendFailure(Component.literal("[NPC] '" + name + "' already exists"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        Vec2 rot = source.getRotation();

        FakePlayer npc = FakePlayer.spawn(
            source.getServer(), level, name,
            pos.x, pos.y, pos.z, rot.y, rot.x
        );
        FakePlayerManager.add(name, npc);

        source.sendSuccess(() -> Component.literal("[NPC] Spawned '" + name + "'"), true);
        return 1;
    }

    private static int npcRemove(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (FakePlayerManager.remove(name)) {
            ctx.getSource().sendSuccess(
                () -> Component.literal("[NPC] Removed '" + name + "'"), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("[NPC] '" + name + "' not found"));
        return 0;
    }

    private static int npcRemoveAll(CommandContext<CommandSourceStack> ctx) {
        int count = FakePlayerManager.getAll().size();
        FakePlayerManager.removeAll();
        ctx.getSource().sendSuccess(
            () -> Component.literal("[NPC] Removed " + count + " NPC(s)"), true);
        return count;
    }

    private static int npcList(CommandContext<CommandSourceStack> ctx) {
        Collection<FakePlayer> npcs = FakePlayerManager.getAll();
        if (npcs.isEmpty()) {
            ctx.getSource().sendSuccess(
                () -> Component.literal("[NPC] No active NPCs"), false);
            return 0;
        }
        for (FakePlayer npc : npcs) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                String.format("[NPC] %s at (%.1f, %.1f, %.1f)",
                    npc.getGameProfile().name(),
                    npc.getX(), npc.getY(), npc.getZ())), false);
        }
        return npcs.size();
    }

    private static int npcGive(CommandContext<CommandSourceStack> ctx, InteractionHand hand)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        FakePlayer npc = FakePlayerManager.get(name);
        if (npc == null) {
            ctx.getSource().sendFailure(Component.literal("[NPC] '" + name + "' not found"));
            return 0;
        }

        ItemInput itemInput = ItemArgument.getItem(ctx, "item");
        ItemStack stack = itemInput.createItemStack(1, false);
        npc.setItemInHand(hand, stack);

        String handName = hand == InteractionHand.MAIN_HAND ? "main hand" : "off hand";
        ctx.getSource().sendSuccess(() -> Component.literal(
            "[NPC] Gave " + stack.getDisplayName().getString() + " to " + name +
            " (" + handName + ")"), true);
        return 1;
    }

    private static int npcShieldDelayGet(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        FakePlayer npc = FakePlayerManager.get(name);
        if (npc == null) {
            ctx.getSource().sendFailure(Component.literal("[NPC] '" + name + "' not found"));
            return 0;
        }
        int ticks = npc.getShieldDisableDuration();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "[NPC] " + name + " shield disable delay: " + ticks + " ticks (" +
            String.format("%.1f", ticks / 20.0) + "s)"), false);
        return 1;
    }

    private static int npcShieldDelaySet(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        FakePlayer npc = FakePlayerManager.get(name);
        if (npc == null) {
            ctx.getSource().sendFailure(Component.literal("[NPC] '" + name + "' not found"));
            return 0;
        }
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        npc.setShieldDisableDuration(ticks);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "[NPC] " + name + " shield disable delay set to " + ticks + " ticks (" +
            String.format("%.1f", ticks / 20.0) + "s)"), true);
        return 1;
    }

    private static int npcUse(CommandContext<CommandSourceStack> ctx, InteractionHand hand) {
        String name = StringArgumentType.getString(ctx, "name");
        FakePlayer npc = FakePlayerManager.get(name);
        if (npc == null) {
            ctx.getSource().sendFailure(Component.literal("[NPC] '" + name + "' not found"));
            return 0;
        }

        boolean wasUsing = npc.isContinuousUse(hand);
        npc.setContinuousUse(hand, !wasUsing);

        String handName = hand == InteractionHand.MAIN_HAND ? "main hand" : "off hand";
        String state = wasUsing ? "stopped" : "started";
        ctx.getSource().sendSuccess(() -> Component.literal(
            "[NPC] " + name + " " + state + " using " + handName), true);
        return 1;
    }
}
