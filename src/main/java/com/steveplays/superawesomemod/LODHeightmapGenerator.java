package com.steveplays.superawesomemod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Optional;

/**
 * Runs on a background daemon thread. Creates a ChunkGenerator from a seed
 * and samples terrain heights for LOD rendering.
 */
public final class LODHeightmapGenerator {

    private static volatile Thread workerThread;
    private static volatile boolean running;

    private static final LevelHeightAccessor OVERWORLD_HEIGHT = new LevelHeightAccessor() {
        @Override public int getHeight() { return 384; }
        @Override public int getMinY() { return -64; }
    };

    private LODHeightmapGenerator() {}

    public static void start(long seed, RegistryAccess registries) {
        stop();
        running = true;
        workerThread = new Thread(() -> runLoop(seed, registries), "LOD-HeightGen");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public static void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
        LODChunkData.clear();
    }

    public static boolean isRunning() {
        return running && workerThread != null && workerThread.isAlive();
    }

    private static void runLoop(long seed, RegistryAccess registries) {
        ChunkGenerator generator;
        RandomState randomState;
        try {
            // Look up the overworld generator from the NORMAL world preset.
            Registry<WorldPreset> presetRegistry = registries.lookupOrThrow(Registries.WORLD_PRESET);
            Optional<Holder.Reference<WorldPreset>> presetOpt = presetRegistry.get(WorldPresets.NORMAL);
            if (presetOpt.isEmpty()) {
                SuperAwesomeMod.LOGGER.error("[LOD] Could not find NORMAL world preset");
                return;
            }
            WorldPreset preset = presetOpt.get().value();

            // Get the overworld LevelStem from the preset.
            Optional<LevelStem> stemOpt = preset.overworld();
            if (stemOpt.isEmpty()) {
                SuperAwesomeMod.LOGGER.error("[LOD] Could not find overworld stem in preset");
                return;
            }
            generator = stemOpt.get().generator();

            // Create RandomState from the noise settings used by the generator.
            if (generator instanceof NoiseBasedChunkGenerator noiseGen) {
                Holder<NoiseGeneratorSettings> settingsHolder = noiseGen.generatorSettings();
                ResourceKey<NoiseGeneratorSettings> settingsKey = settingsHolder.unwrapKey()
                    .orElse(NoiseGeneratorSettings.OVERWORLD);
                randomState = RandomState.create(registries, settingsKey, seed);
            } else {
                SuperAwesomeMod.LOGGER.error("[LOD] Generator is not noise-based, cannot sample heights");
                return;
            }
        } catch (Exception e) {
            SuperAwesomeMod.LOGGER.error("[LOD] Failed to initialize generator", e);
            return;
        }

        SuperAwesomeMod.LOGGER.info("[LOD] Height generator started (seed: {})", seed);

        int lastPcx = Integer.MIN_VALUE;
        int lastPcz = Integer.MIN_VALUE;

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                int pcx = LODChunkData.getPlayerChunkX();
                int pcz = LODChunkData.getPlayerChunkZ();
                int radius = LODChunkData.getLodRadius();
                int skipRadius = Math.min(radius, 12); // don't generate where server sends real chunks

                // Only regenerate ring if player moved significantly.
                boolean moved = Math.abs(pcx - lastPcx) > 2 || Math.abs(pcz - lastPcz) > 2;
                if (moved) {
                    lastPcx = pcx;
                    lastPcz = pcz;
                    LODChunkData.evict(pcx, pcz, radius);
                }

                int generated = 0;
                for (int r = skipRadius; r <= radius && generated < 128 && running; r++) {
                    for (int dx = -r; dx <= r && generated < 128 && running; dx++) {
                        for (int dz = -r; dz <= r && generated < 128 && running; dz++) {
                            // Only process the ring edge at distance r.
                            if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                            int cx = pcx + dx;
                            int cz = pcz + dz;
                            if (LODChunkData.get(cx, cz) != null) continue;

                            int[] heights = sampleChunk(generator, randomState, cx, cz);
                            LODChunkData.put(cx, cz, heights);
                            generated++;
                        }
                    }
                }

                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                SuperAwesomeMod.LOGGER.error("[LOD] Error in generation loop", e);
                try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
            }
        }

        SuperAwesomeMod.LOGGER.info("[LOD] Height generator stopped");
    }

    private static int[] sampleChunk(ChunkGenerator generator, RandomState randomState, int chunkX, int chunkZ) {
        int[] heights = new int[LODChunkData.SAMPLES_PER_CHUNK];
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (int sx = 0; sx < LODChunkData.SAMPLES_PER_AXIS; sx++) {
            for (int sz = 0; sz < LODChunkData.SAMPLES_PER_AXIS; sz++) {
                int blockX = baseX + sx * LODChunkData.SAMPLE_SPACING + 2; // center of 4x4 area
                int blockZ = baseZ + sz * LODChunkData.SAMPLE_SPACING + 2;
                int height = generator.getBaseHeight(
                    blockX, blockZ,
                    Heightmap.Types.WORLD_SURFACE,
                    OVERWORLD_HEIGHT,
                    randomState
                );
                heights[sx * LODChunkData.SAMPLES_PER_AXIS + sz] = height;
            }
        }
        return heights;
    }
}
