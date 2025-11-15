package com.escape.banishmentmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BanishCommand {

    // ========================================================
    //  REGISTER COMMANDS
    // ========================================================
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /banish <player>
        dispatcher.register(
                Commands.literal("banish")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    CommandSourceStack src = ctx.getSource();
                                    ModConfig cfg = ModConfig.INSTANCE;

                                    src.sendSuccess(() ->
                                                    Component.literal("Starting banishment countdown...")
                                                            .withStyle(ChatFormatting.RED),
                                            false
                                    );

                                    CountdownManager.startCountdown(
                                            target,
                                            cfg.countdown,
                                            () -> teleportPlayer(target, cfg, src)
                                    );

                                    return 1;
                                })
                        )
        );


        // /banishconfig
        dispatcher.register(
                Commands.literal("banishconfig")
                        .requires(src -> src.hasPermission(2))

                        // ============================
                        // /banishconfig set ...
                        // ============================
                        .then(Commands.literal("set")

                                // LOCATION OVERRIDE (3 PARAMETERS)
                                .then(Commands.literal("location")
                                        .then(Commands.argument("x", StringArgumentType.word())
                                                .then(Commands.argument("y", StringArgumentType.word())
                                                        .then(Commands.argument("z", StringArgumentType.word())
                                                                .executes(ctx -> {
                                                                    int x = Integer.parseInt(StringArgumentType.getString(ctx, "x"));
                                                                    int y = Integer.parseInt(StringArgumentType.getString(ctx, "y"));
                                                                    int z = Integer.parseInt(StringArgumentType.getString(ctx, "z"));

                                                                    ModConfig.INSTANCE.location = new BlockPos(x, y, z);
                                                                    ModConfig.save();

                                                                    ctx.getSource().sendSuccess(() ->
                                                                                    Component.literal("Location set to: " + x + " " + y + " " + z)
                                                                                            .withStyle(ChatFormatting.GREEN),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )

                                // SINGLE-VALUE OPTIONS
                                .then(Commands.argument("option", StringArgumentType.word())
                                        .suggests(BanishCommand::suggestOptions)
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .suggests(BanishCommand::suggestValues)
                                                .executes(BanishCommand::setConfigOption)
                                        )
                                )
                        )

                        // ============================
                        // /banishconfig view
                        // ============================
                        .then(Commands.literal("view")
                                .executes(ctx -> {
                                    viewConfig(ctx.getSource());
                                    return 1;
                                })
                        )

                        // ============================
                        // /banishconfig reload
                        // ============================
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    ModConfig.loadSafe();
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Banish config reloaded."), false);
                                    return 1;
                                })
                        )
        );
    }

    // ========================================================
    //  SUGGESTIONS
    // ========================================================
    private static CompletableFuture<Suggestions> suggestOptions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        List<String> opts = Arrays.asList(
                "countdown", "highnote", "lownote", "noteinterval",
                "userandom", "mindistance", "maxdistance"
        );

        for (String s : opts)
            if (s.startsWith(b.getRemainingLowerCase()))
                b.suggest(s);

        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestValues(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder b) {
        String opt = StringArgumentType.getString(ctx, "option").toLowerCase();

        if (opt.equals("userandom")) {
            b.suggest("true");
            b.suggest("false");
        }

        return b.buildFuture();
    }

    // ========================================================
    //  SET CONFIG OPTION
    // ========================================================
    private static int setConfigOption(CommandContext<CommandSourceStack> ctx) {
        String option = StringArgumentType.getString(ctx, "option").toLowerCase();
        String value = StringArgumentType.getString(ctx, "value");

        try {
            switch (option) {
                case "countdown":
                    ModConfig.INSTANCE.countdown = Integer.parseInt(value);
                    break;

                case "highnote":
                    ModConfig.INSTANCE.highNotePitch = Float.parseFloat(value);
                    break;

                case "lownote":
                    ModConfig.INSTANCE.lowNotePitch = Float.parseFloat(value);
                    break;

                case "noteinterval":
                    ModConfig.INSTANCE.noteInterval = Integer.parseInt(value);
                    break;

                case "userandom":
                    ModConfig.INSTANCE.useRandomOffset = value.equalsIgnoreCase("true");
                    break;

                case "mindistance":
                    ModConfig.INSTANCE.minDistance = Integer.parseInt(value);
                    break;

                case "maxdistance":
                    ModConfig.INSTANCE.maxDistance = Integer.parseInt(value);
                    break;

                default:
                    ctx.getSource().sendFailure(Component.literal("Unknown option: " + option));
                    return 0;
            }

            ModConfig.save();
            ctx.getSource().sendSuccess(() ->
                            Component.literal("Updated " + option + " to " + value)
                                    .withStyle(ChatFormatting.GREEN),
                    false);

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid value: " + e.getMessage()));
            return 0;
        }
    }

    // ========================================================
    //  VIEW CONFIG
    // ========================================================
    private static void viewConfig(CommandSourceStack src) {
        ModConfig c = ModConfig.INSTANCE;

        src.sendSuccess(() -> Component.literal("--- Banish Config ---").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> Component.literal("Location: " + c.location), false);
        src.sendSuccess(() -> Component.literal("Countdown: " + c.countdown), false);
        src.sendSuccess(() -> Component.literal("High Note Pitch: " + c.highNotePitch), false);
        src.sendSuccess(() -> Component.literal("Low Note Pitch: " + c.lowNotePitch), false);
        src.sendSuccess(() -> Component.literal("Note Interval: " + c.noteInterval), false);
        src.sendSuccess(() -> Component.literal("Use Random Offset: " + c.useRandomOffset), false);
        src.sendSuccess(() -> Component.literal("Min Dist: " + c.minDistance), false);
        src.sendSuccess(() -> Component.literal("Max Dist: " + c.maxDistance), false);
    }

    // ========================================================
    //  TELEPORT PLAYER
    // ========================================================
    private static void teleportPlayer(ServerPlayer target, ModConfig cfg, CommandSourceStack src) {

        ServerLevel world = target.level();
        BlockPos dest;

        if (cfg.useRandomOffset) {
            dest = PositionFinder.findRandomSafePosition(world, target, cfg.minDistance, cfg.maxDistance);
        } else {
            dest = cfg.location;
        }

        if (dest == null) {
            src.sendFailure(Component.literal("Failed to find safe teleport location."));
            return;
        }

        // Teleport
        target.teleportTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
   ;             target.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        target.sendSystemMessage(
                Component.literal("You have been BANISHED!")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
        );
    }
}
