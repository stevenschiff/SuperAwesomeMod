package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Records the distance of every swing you take at something, and whether the server
 * confirmed it.
 *
 * <p>The gap is measured on <em>our own swing</em>, not on the arriving damage packet.
 * {@code PvpDetectorTracker} measures on the packet and says so in its own comment
 * ("use current interpolated pos — best we have"), which is fine for spotting a
 * five-block reach hack. It is not fine for teaching: at any real ping both players
 * have moved by the time the packet lands, and the error lands squarely in the
 * fraction-of-a-block range this is meant to train. Measuring at swing time is exact.
 *
 * <p>Confirmation is the softer number. A missing damage event does not prove the
 * swing was out of reach — invulnerability frames, a raised shield and a target dying
 * all suppress it. So it is reported as "confirmed", never as "hit rate".
 */
public final class SpacingTracker {

    /** 0.25-block buckets across the widest reach we expect to see. */
    public static final double BUCKET_SIZE = 0.25;
    public static final int BUCKET_COUNT = 16;

    private static final long MIN_CONFIRM_WAIT_MS = 250L;

    private static final class Pending {
        final int victimId;
        final long atMs;
        Pending(int victimId, long atMs) {
            this.victimId = victimId;
            this.atMs = atMs;
        }
    }

    private static final Deque<Pending> PENDING = new ArrayDeque<>();
    private static final int[] BUCKETS = new int[BUCKET_COUNT];

    private static int swings = 0;
    private static int confirmed = 0;
    private static double gapSum = 0.0;
    private static double gapMin = Double.NaN;
    private static double gapMax = Double.NaN;
    private static int inBandCount = 0;

    private static double lastGap = Double.NaN;
    private static long lastGapMs = 0L;

    private SpacingTracker() {}

    /** Called the moment we commit to an attack, before any of it reaches the server. */
    public static void recordSwing(Entity target) {
        if (!SpacingData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        Player self = mc.player;
        if (self == null || target == null || target == self) return;

        double gap = CombatReach.gap(self.getEyePosition(), target.getBoundingBox());
        double reach = CombatReach.attackReach(self);

        swings++;
        gapSum += gap;
        gapMin = Double.isNaN(gapMin) ? gap : Math.min(gapMin, gap);
        gapMax = Double.isNaN(gapMax) ? gap : Math.max(gapMax, gap);
        if (SpacingData.zoneOf(gap, reach) == SpacingData.Zone.GOOD) inBandCount++;

        int bucket = Math.clamp((int) (gap / BUCKET_SIZE), 0, BUCKET_COUNT - 1);
        BUCKETS[bucket]++;

        lastGap = gap;
        lastGapMs = System.currentTimeMillis();

        PENDING.addLast(new Pending(target.getId(), lastGapMs));
    }

    /** Called when the server tells us something took damage from us. */
    public static void confirmSwing(int attackerId, int victimId) {
        if (!SpacingData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || attackerId != mc.player.getId()) return;

        for (var it = PENDING.iterator(); it.hasNext(); ) {
            Pending p = it.next();
            if (p.victimId == victimId) {
                it.remove();
                confirmed++;
                return;
            }
        }
    }

    /** Drops swings the server never acknowledged. Call once per client tick. */
    public static void tick(Minecraft client) {
        if (PENDING.isEmpty()) return;

        long wait = Math.max(MIN_CONFIRM_WAIT_MS, 2L * pingOf(client));
        long cutoff = System.currentTimeMillis() - wait;
        while (!PENDING.isEmpty() && PENDING.peekFirst().atMs < cutoff) {
            PENDING.pollFirst();
        }
    }

    public static void reset() {
        PENDING.clear();
        java.util.Arrays.fill(BUCKETS, 0);
        swings = 0;
        confirmed = 0;
        gapSum = 0.0;
        gapMin = Double.NaN;
        gapMax = Double.NaN;
        inBandCount = 0;
        lastGap = Double.NaN;
        lastGapMs = 0L;
    }

    // --- Readouts --------------------------------------------------------

    public static int    getSwings()     { return swings; }
    public static int    getConfirmed()  { return confirmed; }
    public static double getMeanGap()    { return swings == 0 ? 0.0 : gapSum / swings; }
    public static double getMinGap()     { return Double.isNaN(gapMin) ? 0.0 : gapMin; }
    public static double getMaxGap()     { return Double.isNaN(gapMax) ? 0.0 : gapMax; }
    public static int[]  getBuckets()    { return BUCKETS; }

    public static double getInBandPercent() {
        return swings == 0 ? 0.0 : (inBandCount * 100.0) / swings;
    }

    public static double getConfirmedPercent() {
        return swings == 0 ? 0.0 : (confirmed * 100.0) / swings;
    }

    /** Gap of the most recent swing, or NaN if there wasn't one. */
    public static double getLastGap() { return lastGap; }

    /** How long ago that swing was, for the fading flash. */
    public static long millisSinceLastSwing() {
        return lastGapMs == 0L ? Long.MAX_VALUE : System.currentTimeMillis() - lastGapMs;
    }

    private static int pingOf(Minecraft client) {
        if (client.player == null || client.getConnection() == null) return 0;
        PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }
}
