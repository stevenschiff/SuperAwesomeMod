package com.steveplays.superawesomemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MiniMapPersistence {

    private MiniMapPersistence() {}

    private static boolean dirty = false;
    private static int ticksSinceLastSave = 0;
    private static final int SAVE_INTERVAL_TICKS = 600; // 30 seconds

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void markDirty() {
        dirty = true;
    }

    public static void tick() {
        if (!dirty) return;
        ticksSinceLastSave++;
        if (ticksSinceLastSave >= SAVE_INTERVAL_TICKS) {
            save();
            dirty = false;
            ticksSinceLastSave = 0;
        }
    }

    // --- World lifecycle ---

    public static void onWorldJoin(Minecraft mc) {
        String worldId = resolveWorldId(mc);
        MiniMapData.setCurrentWorldId(worldId);
        MiniMapChunkCache.clear();
        loadChunks();
        loadWaypoints();
        dirty = false;
        ticksSinceLastSave = 0;
    }

    public static void onWorldLeave() {
        save();
        MiniMapChunkCache.clear();
        MiniMapData.clearWaypoints();
        MiniMapData.setCurrentWorldId("");
        dirty = false;
        ticksSinceLastSave = 0;
    }

    public static void save() {
        if (MiniMapData.getCurrentWorldId().isEmpty()) return;
        saveChunks();
        saveWaypoints();
    }

    // --- World ID resolution ---

    private static String resolveWorldId(Minecraft mc) {
        if (mc.getSingleplayerServer() != null) {
            // Singleplayer: use world folder name
            try {
                Path worldPath = mc.getSingleplayerServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT);
                return worldPath.getParent().getFileName().toString();
            } catch (Exception e) {
                return "singleplayer";
            }
        } else if (mc.getCurrentServer() != null) {
            // Multiplayer: sanitize server address
            return mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        }
        return "unknown";
    }

    // --- Paths ---

    private static Path getBaseDir() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gameDirectory.toPath()
            .resolve("superawesomemod")
            .resolve("minimap")
            .resolve(MiniMapData.getCurrentWorldId());
    }

    // --- Chunk persistence (binary, per-dimension) ---

    private static void saveChunks() {
        try {
            Path dir = getBaseDir();
            Files.createDirectories(dir);

            // Save each dimension to its own file
            for (String dimension : MiniMapChunkCache.getDimensions()) {
                var cache = MiniMapChunkCache.getForDimension(dimension);
                if (cache.isEmpty()) continue;

                Path file = dir.resolve("chunks_" + dimension + ".dat");
                Path temp = dir.resolve("chunks_" + dimension + ".dat.tmp");

                try (DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(Files.newOutputStream(temp)))) {
                    out.writeInt(1); // version
                    out.writeInt(cache.size());

                    cache.forEach((key, colors) -> {
                        try {
                            out.writeInt(MiniMapChunkCache.unpackX(key));
                            out.writeInt(MiniMapChunkCache.unpackZ(key));
                            for (int color : colors) {
                                out.writeInt(color);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }

                Files.deleteIfExists(file);
                Files.move(temp, file);
            }

            // Clean up old single-file format if it exists
            Path oldFile = dir.resolve("chunks.dat");
            Files.deleteIfExists(oldFile);

        } catch (Exception e) {
            SuperAwesomeMod.LOGGER.warn("[MiniMap] Failed to save chunk data", e);
        }
    }

    private static void loadChunks() {
        Path dir = getBaseDir();
        if (!Files.exists(dir)) return;

        // Load per-dimension files
        String[] dimensions = {"overworld", "the_nether", "the_end"};
        for (String dimension : dimensions) {
            Path file = dir.resolve("chunks_" + dimension + ".dat");
            if (!Files.exists(file)) continue;

            try (DataInputStream in = new DataInputStream(
                    new BufferedInputStream(Files.newInputStream(file)))) {
                int version = in.readInt();
                if (version != 1) continue;

                int count = in.readInt();
                for (int i = 0; i < count; i++) {
                    int chunkX = in.readInt();
                    int chunkZ = in.readInt();
                    int[] colors = new int[256];
                    for (int j = 0; j < 256; j++) {
                        colors[j] = in.readInt();
                    }
                    MiniMapChunkCache.putForDimension(dimension, chunkX, chunkZ, colors);
                }

                SuperAwesomeMod.LOGGER.info("[MiniMap] Loaded {} chunks for dimension '{}'", count, dimension);

            } catch (Exception e) {
                SuperAwesomeMod.LOGGER.warn("[MiniMap] Failed to load chunks for dimension '{}'", dimension, e);
            }
        }

        // Migrate old single-file format (treat as overworld)
        Path oldFile = dir.resolve("chunks.dat");
        if (Files.exists(oldFile)) {
            try (DataInputStream in = new DataInputStream(
                    new BufferedInputStream(Files.newInputStream(oldFile)))) {
                int version = in.readInt();
                if (version == 1) {
                    int count = in.readInt();
                    for (int i = 0; i < count; i++) {
                        int chunkX = in.readInt();
                        int chunkZ = in.readInt();
                        int[] colors = new int[256];
                        for (int j = 0; j < 256; j++) {
                            colors[j] = in.readInt();
                        }
                        MiniMapChunkCache.putForDimension("overworld", chunkX, chunkZ, colors);
                    }
                    SuperAwesomeMod.LOGGER.info("[MiniMap] Migrated {} old chunks to overworld", count);
                }
            } catch (Exception e) {
                SuperAwesomeMod.LOGGER.warn("[MiniMap] Failed to migrate old chunk data", e);
            }
        }
    }

    // --- Waypoint persistence (JSON) ---

    private static void saveWaypoints() {
        try {
            Path dir = getBaseDir();
            Files.createDirectories(dir);
            Path file = dir.resolve("waypoints.json");

            JsonArray arr = new JsonArray();
            for (MiniMapWaypoint wp : MiniMapData.getWaypoints()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", wp.name());
                obj.addProperty("x", wp.x());
                obj.addProperty("z", wp.z());
                obj.addProperty("color", wp.color());
                obj.addProperty("visible", wp.visible());
                obj.addProperty("dimension", wp.dimension());
                arr.add(obj);
            }

            Files.writeString(file, GSON.toJson(arr));

        } catch (Exception e) {
            SuperAwesomeMod.LOGGER.warn("[MiniMap] Failed to save waypoints", e);
        }
    }

    private static void loadWaypoints() {
        Path file = getBaseDir().resolve("waypoints.json");
        if (!Files.exists(file)) return;

        try {
            String json = Files.readString(file);
            JsonArray arr = GSON.fromJson(json, JsonArray.class);
            if (arr == null) return;

            List<MiniMapWaypoint> wps = new ArrayList<>();
            for (var elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String name = obj.get("name").getAsString();
                int x = obj.get("x").getAsInt();
                int z = obj.get("z").getAsInt();
                int color = obj.get("color").getAsInt();
                boolean visible = obj.has("visible") ? obj.get("visible").getAsBoolean() : true;
                String dimension = obj.has("dimension") ? obj.get("dimension").getAsString() : "overworld";
                wps.add(new MiniMapWaypoint(name, x, z, color, visible, dimension));
            }
            MiniMapData.setWaypoints(wps);

            SuperAwesomeMod.LOGGER.info("[MiniMap] Loaded {} waypoints from disk", wps.size());

        } catch (Exception e) {
            SuperAwesomeMod.LOGGER.warn("[MiniMap] Failed to load waypoints", e);
        }
    }
}
