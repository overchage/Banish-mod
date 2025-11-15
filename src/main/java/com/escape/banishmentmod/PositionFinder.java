package com.escape.banishmentmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class PositionFinder {

    /**
     * Attempts up to 30 times to find a safe random position around the player's current position.
     * Returns null if none found.
     */
    public static BlockPos findRandomSafePosition(ServerLevel level, ServerPlayer player, int minDist, int maxDist) {
        Random r = new Random();

        for (int i = 0; i < 30; i++) {
            double angle = r.nextDouble() * Math.PI * 2;
            int dist = minDist + r.nextInt(Math.max(1, maxDist - minDist + 1));
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);

            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            BlockState below = level.getBlockState(top.below());

            // Ensure there's solid ground and not lava/cactus
            if (!below.isAir() && below.isSolid() && !below.is(Blocks.LAVA) && !below.is(Blocks.CACTUS)) {
                return top;
            }
        }

        return null;
    }
}
