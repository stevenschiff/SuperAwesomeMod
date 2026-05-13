package com.steveplays.superawesomemod;

import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all active fake players (NPCs) by lowercase name.
 * Uses {@link ConcurrentHashMap} for thread safety in singleplayer
 * (client and server threads share the same JVM).
 */
public class FakePlayerManager {

    private static final Map<String, FakePlayer> npcs = new ConcurrentHashMap<>();

    public static void add(String name, FakePlayer fakePlayer) {
        npcs.put(name.toLowerCase(), fakePlayer);
    }

    public static FakePlayer get(String name) {
        return npcs.get(name.toLowerCase());
    }

    public static boolean exists(String name) {
        return npcs.containsKey(name.toLowerCase());
    }

    public static Collection<FakePlayer> getAll() {
        return npcs.values();
    }

    public static Collection<String> getNames() {
        return npcs.keySet();
    }

    public static boolean remove(String name) {
        FakePlayer fp = npcs.remove(name.toLowerCase());
        if (fp == null) return false;

        MinecraftServer server = fp.level().getServer();
        if (server != null) {
            server.getPlayerList().remove(fp);
        }
        return true;
    }

    public static void removeAll() {
        for (FakePlayer fp : npcs.values()) {
            MinecraftServer server = fp.level().getServer();
            if (server != null) {
                server.getPlayerList().remove(fp);
            }
        }
        npcs.clear();
    }
}
