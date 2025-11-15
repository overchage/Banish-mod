package com.escape.banishmentmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Handles countdown logic for the /banish command.
 * NeoForge 1.21.8 compatible.
 */
public class CountdownManager {

    private static boolean active = false;

    private static int ticksRemaining = 0;
    private static int noteTimer = 0;

    private static ServerPlayer targetPlayer = null;
    private static Runnable finishCallback = null;

    // NEW: alternate high/low pitch each beep
    private static boolean toggleHigh = true;

    /**
     * Starts a countdown for the given player.
     */
    public static void startCountdown(ServerPlayer player, int seconds, Runnable callback) {
        if (active) return; // prevent overlap

        active = true;
        targetPlayer = player;
        finishCallback = callback;

        ticksRemaining = Math.max(0, seconds) * 20;
        noteTimer = 0;
        toggleHigh = true;

        // Register this class on the event bus
        NeoForge.EVENT_BUS.register(CountdownManager.class);
    }

    /**
     * Runs every server tick (20 per second).
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!active || targetPlayer == null) return;

        ServerLevel world;
        try {
            world = (ServerLevel) targetPlayer.level();
        } catch (Exception e) {
            return;
        }

        // NOTE SOUND
        noteTimer++;
        int interval = Math.max(1, ModConfig.INSTANCE.noteInterval);

        if (noteTimer >= interval) {
            noteTimer = 0;

            // ★ ALTERNATING NOTE PITCH ★
            float pitch = toggleHigh
                    ? ModConfig.INSTANCE.highNotePitch
                    : ModConfig.INSTANCE.lowNotePitch;

            toggleHigh = !toggleHigh; // flip tone for next beep

            world.playSound(
                    null,
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    pitch
            );
        }

        // COUNTDOWN MESSAGE (every full second)
        if (ticksRemaining % 20 == 0) {
            int seconds = ticksRemaining / 20;
            String text = "Banishing " + targetPlayer.getName().getString() +
                    " in " + seconds + " second" + (seconds == 1 ? "" : "s") + "!";

            world.players().forEach(p ->
                    p.sendSystemMessage(
                            Component.literal(text)
                                    .withStyle(net.minecraft.ChatFormatting.DARK_RED)
                                    .withStyle(net.minecraft.ChatFormatting.BOLD)
                    )
            );
        }


        // Step the countdown
        ticksRemaining--;

        // FINISHED!
        if (ticksRemaining < 0) {
            active = false;

            // Thunder when banished
            world.playSound(
                    null,
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    8.0F,
                    1.0F
            );

            // Run teleport callback
            if (finishCallback != null) {
                try {
                    finishCallback.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            // Cleanup
            NeoForge.EVENT_BUS.unregister(CountdownManager.class);
            targetPlayer = null;
            finishCallback = null;
        }
    }
}
