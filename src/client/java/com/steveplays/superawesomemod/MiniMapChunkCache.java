package com.steveplays.superawesomemod;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class MiniMapChunkCache {

    private MiniMapChunkCache() {}

    // Key = packed (chunkX, chunkZ), value = int[256] of ARGB colors (lx * 16 + lz)
    private static final ConcurrentHashMap<Long, int[]> CACHE = new ConcurrentHashMap<>();

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

    public static void put(int chunkX, int chunkZ, int[] colors) {
        CACHE.put(packKey(chunkX, chunkZ), colors);
    }

    public static boolean contains(int chunkX, int chunkZ) {
        return CACHE.containsKey(packKey(chunkX, chunkZ));
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
}
