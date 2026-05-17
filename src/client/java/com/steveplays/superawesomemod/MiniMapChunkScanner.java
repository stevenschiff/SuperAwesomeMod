package com.steveplays.superawesomemod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

public final class MiniMapChunkScanner {

    private MiniMapChunkScanner() {}

    public static void register() {
        ClientChunkEvents.CHUNK_LOAD.register(MiniMapChunkScanner::onChunkLoad);
    }

    private static void onChunkLoad(ClientLevel level, LevelChunk chunk) {
        if (!MiniMapData.isEnabled()) return;

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Always rescan (terrain may have changed)
        int[] colors = scanChunk(level, chunk);
        MiniMapChunkCache.put(chunkX, chunkZ, colors);
        MiniMapPersistence.markDirty();
    }

    public static void scanLoadedChunks(ClientLevel level) {
        // Scan all currently loaded chunks (used on world join to fill cache)
        // This is called after persistence loads, so we only scan chunks not in cache
        if (level == null) return;
        // Chunks are loaded by the client automatically; we rely on CHUNK_LOAD events
        // This method can force-scan visible chunks if needed
    }

    public static int[] scanChunk(ClientLevel level, LevelChunk chunk) {
        int[] colors = new int[256];
        int baseX = chunk.getPos().x * 16;
        int baseZ = chunk.getPos().z * 16;

        boolean isNether = level.dimension() == Level.NETHER;
        int playerY = -1;
        if (isNether) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                playerY = (int) player.getY();
            }
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int surfaceY;
                if (isNether) {
                    // Scan downward from the player's Y to find the floor
                    surfaceY = findNetherSurface(chunk, lx, lz, baseX, baseZ, mutable, playerY);
                } else {
                    surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
                }

                mutable.set(baseX + lx, surfaceY, baseZ + lz);
                BlockState state = chunk.getBlockState(mutable);
                MapColor mapColor = state.getMapColor(level, mutable);

                if (mapColor != MapColor.NONE) {
                    colors[lx * 16 + lz] = mapColor.col | 0xFF000000;
                } else {
                    colors[lx * 16 + lz] = 0xFF000000; // black for none
                }
            }
        }
        return colors;
    }

    /**
     * In the Nether, scan downward from the player's Y level to find the
     * first solid non-lava block. This gives us the actual floor the player
     * is walking on rather than the ceiling/roof above.
     */
    private static int findNetherSurface(LevelChunk chunk, int lx, int lz,
                                          int baseX, int baseZ,
                                          BlockPos.MutableBlockPos mutable,
                                          int playerY) {
        int startY = (playerY > 0) ? playerY : 80;
        for (int y = startY; y >= 0; y--) {
            mutable.set(baseX + lx, y, baseZ + lz);
            BlockState state = chunk.getBlockState(mutable);
            if (state.isAir() || state.getBlock() == Blocks.LAVA) continue;
            return y;
        }
        return 0;
    }
}
