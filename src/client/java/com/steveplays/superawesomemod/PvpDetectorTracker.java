package com.steveplays.superawesomemod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Records per-player attack observations and computes a heuristic suspicion score.
 *
 * Suspicion model — all values tunable from constants below:
 *   reach <= 3.0          -> 0   (vanilla attack range)
 *   reach <= 3.5          -> 0   (latency tolerance)
 *   reach <= 4.0          -> 1
 *   reach <= 4.5          -> 3
 *   reach <= 5.5          -> 6
 *   reach >  5.5          -> 10
 *   line-of-sight blocked -> +5  (through-wall hit)
 *   high ping (>250ms)    -> score scaled by 0.6 (likely latency artifact, not cheating)
 *
 * Per-attacker score is the sum over a rolling 60-second window. Hits older than that
 * are discarded. Bands: <5 NONE, 5-15 LOW, 15-30 MED, 30+ HIGH.
 */
public final class PvpDetectorTracker {

    // --- Tunables --------------------------------------------------------
    public static final double VANILLA_REACH    = 3.0;
    public static final double LATENCY_TOLERANCE = 0.5;   // grace before any score
    public static final long   WINDOW_MS         = 60_000L;
    public static final int    MAX_HITS_PER_PLAYER = 64;

    public static final int LOW_BAND  = 5;
    public static final int MED_BAND  = 15;
    public static final int HIGH_BAND = 30;
    // --------------------------------------------------------------------

    public static final class HitRecord {
        public final long   timestampMs;
        public final double reach;
        public final boolean losBlocked;
        public final int    ping;
        public final double score;

        HitRecord(long timestampMs, double reach, boolean losBlocked, int ping, double score) {
            this.timestampMs = timestampMs;
            this.reach       = reach;
            this.losBlocked  = losBlocked;
            this.ping        = ping;
            this.score       = score;
        }
    }

    public static final class PlayerStats {
        public final UUID   uuid;
        public final String name;
        public final Deque<HitRecord> hits = new ArrayDeque<>();
        public int    pingMs;

        // Session-wide cumulative count of hits flagged as suspicious (score > 0).
        // Does NOT decay with the rolling window — it's the answer to
        // "how many times has this player been suspected this session?"
        public int    totalSuspectCount;
        public long   lastSuspectMs;

        PlayerStats(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public int    hitCount()      { return hits.size(); }
        public int    suspectCount()  { return totalSuspectCount; }
        public double maxReach()  { return hits.stream().mapToDouble(h -> h.reach).max().orElse(0.0); }
        public double avgReach()  { return hits.stream().mapToDouble(h -> h.reach).average().orElse(0.0); }
        public double totalScore(){ return hits.stream().mapToDouble(h -> h.score).sum(); }
        public long   lastHitMs() {
            long h = hits.isEmpty() ? 0L : hits.peekLast().timestampMs;
            return Math.max(h, lastSuspectMs);
        }

        public Band band() {
            double s = totalScore();
            if (s >= HIGH_BAND) return Band.HIGH;
            if (s >= MED_BAND)  return Band.MED;
            if (s >= LOW_BAND)  return Band.LOW;
            return Band.NONE;
        }
    }

    public enum Band { NONE, LOW, MED, HIGH }

    private static final Map<UUID, PlayerStats> STATS = new HashMap<>();

    private PvpDetectorTracker() {}

    /** Called from the damage-event mixin on the main thread. */
    public static void recordHit(int attackerEntityId, int victimEntityId) {
        if (!PvpDetectorData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        Entity attacker = level.getEntity(attackerEntityId);
        Entity victim   = level.getEntity(victimEntityId);
        if (!(attacker instanceof Player) || victim == null) return;
        if (attacker == mc.player) return; // don't track yourself

        Player attackerPlayer = (Player) attacker;

        // Eye position at attack time — use current interpolated pos (best we have).
        Vec3 eye = attackerPlayer.getEyePosition();
        AABB box = victim.getBoundingBox();
        double reach = Math.sqrt(nearestPointDistanceSqr(eye, box));

        boolean losBlocked = isLineOfSightBlocked(level, eye, box.getCenter(), attackerPlayer);

        int ping = lookupPing(mc, attackerPlayer.getUUID());

        double score = computeScore(reach, losBlocked, ping);

        PlayerStats st = STATS.computeIfAbsent(attackerPlayer.getUUID(),
            u -> new PlayerStats(u, attackerPlayer.getName().getString()));
        st.pingMs = ping;
        long now = System.currentTimeMillis();
        st.hits.addLast(new HitRecord(now, reach, losBlocked, ping, score));
        if (score > 0) {
            st.totalSuspectCount++;
            st.lastSuspectMs = now;
        }

        // Bound memory.
        while (st.hits.size() > MAX_HITS_PER_PLAYER) st.hits.pollFirst();
    }

    /** Drop expired hits and players that have nothing left. Call once per render frame. */
    public static void tick() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        var it = STATS.values().iterator();
        while (it.hasNext()) {
            PlayerStats st = it.next();
            while (!st.hits.isEmpty() && st.hits.peekFirst().timestampMs < cutoff) {
                st.hits.pollFirst();
            }
            // Keep entries that have ever been flagged so the session-wide
            // suspect counter remains visible after the rolling window expires.
            if (st.hits.isEmpty() && st.totalSuspectCount == 0) it.remove();
        }
    }

    /** Snapshot for HUD rendering, sorted by recency (most recent first). */
    public static List<PlayerStats> snapshot() {
        List<PlayerStats> out = new ArrayList<>(STATS.values());
        out.sort(Comparator.comparingLong(PlayerStats::lastHitMs).reversed());
        return out;
    }

    public static void clear() { STATS.clear(); }

    // --- Helpers ---------------------------------------------------------

    private static double computeScore(double reach, boolean losBlocked, int ping) {
        double s;
        if (reach <= VANILLA_REACH + LATENCY_TOLERANCE) s = 0;
        else if (reach <= 4.0) s = 1;
        else if (reach <= 4.5) s = 3;
        else if (reach <= 5.5) s = 6;
        else                   s = 10;

        if (losBlocked) s += 5;
        if (ping > 250) s *= 0.6; // dampen on high latency

        return s;
    }

    private static double nearestPointDistanceSqr(Vec3 p, AABB b) {
        double dx = Math.max(0.0, Math.max(b.minX - p.x, p.x - b.maxX));
        double dy = Math.max(0.0, Math.max(b.minY - p.y, p.y - b.maxY));
        double dz = Math.max(0.0, Math.max(b.minZ - p.z, p.z - b.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean isLineOfSightBlocked(ClientLevel level, Vec3 from, Vec3 to, Entity ignore) {
        BlockHitResult hit = level.clip(new ClipContext(
            from, to,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            ignore));
        return hit.getType() == BlockHitResult.Type.BLOCK;
    }

    private static int lookupPing(Minecraft mc, UUID uuid) {
        if (mc.getConnection() == null) return -1;
        PlayerInfo info = mc.getConnection().getPlayerInfo(uuid);
        return info == null ? -1 : info.getLatency();
    }
}
