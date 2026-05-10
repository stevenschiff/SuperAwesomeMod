package com.steveplays.superawesomemod;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Thread-safe cache of LOD heightmap data.
 * Each entry stores a 4x4 grid of height samples for one chunk (sample every 4 blocks).
 */
public final class LODChunkData {

    public static final int SAMPLES_PER_AXIS = 4;
    public static final int SAMPLE_SPACING = 4; // blocks between samples
    public static final int SAMPLES_PER_CHUNK = SAMPLES_PER_AXIS * SAMPLES_PER_AXIS;

    private static final ConcurrentHashMap<Long, int[]> CACHE = new ConcurrentHashMap<>();

    private static volatile int playerChunkX;
    private static volatile int playerChunkZ;
    private static volatile int lodRadius = 64;
    private static volatile int skipRadius = 12;

    private LODChunkData() {}

    public static long packKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public static int unpackX(long key) {
        return (int) (key >> 32);
    }

    public static int unpackZ(long key) {
        return (int) key;
    }

    public static int[] get(int chunkX, int chunkZ) {
        return CACHE.get(packKey(chunkX, chunkZ));
    }

    public static void put(int chunkX, int chunkZ, int[] heights) {
        CACHE.put(packKey(chunkX, chunkZ), heights);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static int size() {
        return CACHE.size();
    }

    public static void forEach(BiConsumer<Long, int[]> consumer) {
        CACHE.forEach(consumer);
    }

    /** Remove entries too far from the player. */
    public static void evict(int centerChunkX, int centerChunkZ, int maxRadius) {
        CACHE.entrySet().removeIf(entry -> {
            int cx = unpackX(entry.getKey());
            int cz = unpackZ(entry.getKey());
            int dx = cx - centerChunkX;
            int dz = cz - centerChunkZ;
            return dx * dx + dz * dz > (maxRadius + 16) * (maxRadius + 16);
        });
    }

    public static int getPlayerChunkX() { return playerChunkX; }
    public static int getPlayerChunkZ() { return playerChunkZ; }
    public static void setPlayerPos(int cx, int cz) { playerChunkX = cx; playerChunkZ = cz; }

    public static int getLodRadius() { return lodRadius; }
    public static void setLodRadius(int r) { lodRadius = r; }

    public static int getSkipRadius() { return skipRadius; }
    public static void setSkipRadius(int r) { skipRadius = r; }
}
