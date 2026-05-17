package com.steveplays.superawesomemod;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class MiniMapChunkCache {

    private MiniMapChunkCache() {}

    // Per-dimension caches: dimension -> (packed chunkX/Z -> colors)
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, int[]>> CACHES =
        new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Long, int[]> currentCache() {
        String dim = MiniMapData.getCurrentDimension();
        if (dim.isEmpty()) dim = "overworld";
        return CACHES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
    }

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
        return currentCache().get(packKey(chunkX, chunkZ));
    }

    public static void put(int chunkX, int chunkZ, int[] colors) {
        currentCache().put(packKey(chunkX, chunkZ), colors);
    }

    public static boolean contains(int chunkX, int chunkZ) {
        return currentCache().containsKey(packKey(chunkX, chunkZ));
    }

    public static void clear() {
        CACHES.clear();
    }

    public static int size() {
        return currentCache().size();
    }

    public static void forEach(BiConsumer<Long, int[]> consumer) {
        currentCache().forEach(consumer);
    }

    // --- Persistence helpers for saving/loading all dimensions ---

    public static Set<String> getDimensions() {
        return CACHES.keySet();
    }

    public static ConcurrentHashMap<Long, int[]> getForDimension(String dimension) {
        return CACHES.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
    }

    public static void putForDimension(String dimension, int chunkX, int chunkZ, int[] colors) {
        CACHES.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>())
              .put(packKey(chunkX, chunkZ), colors);
    }
}
