package com.steveplays.superawesomemod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Turns the player toward a point the way a hand would, not the way a script would.
 *
 * <p>Snapping yaw straight to the target is the single clearest tell that nobody is
 * holding the mouse, so the turn is eased: each tick covers a fraction of what is left,
 * capped at a maximum step, which gives a fast start and a soft finish. A little jitter
 * is added while turning and the aim point is nudged off the exact block centre, because
 * a camera that settles on a mathematically perfect pixel is its own giveaway.
 *
 * <p>Rotation is applied to the player's real yaw and pitch, so it travels to the server
 * in ordinary movement packets.
 */
public final class RotationHelper {

    /** Hard ceiling on a single tick's turn, in degrees. */
    private static final float MAX_STEP = 22f;
    /** Fraction of the remaining angle covered per tick before the cap applies. */
    private static final float EASE = 0.35f;

    private static final Random RANDOM = new Random();

    private RotationHelper() {}

    /** A point inside the block, off-centre, so repeated mining isn't pixel-identical. */
    public static Vec3 humanAimPoint(double x, double y, double z) {
        return new Vec3(
            x + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.45,
            y + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.45,
            z + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.45);
    }

    /** Yaw the player would need to face the point. */
    public static float yawTo(LocalPlayer player, Vec3 point) {
        Vec3 eye = player.getEyePosition();
        double dx = point.x - eye.x;
        double dz = point.z - eye.z;
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    /** Pitch the player would need to face the point. */
    public static float pitchTo(LocalPlayer player, Vec3 point) {
        Vec3 eye = player.getEyePosition();
        double dx = point.x - eye.x;
        double dy = point.y - eye.y;
        double dz = point.z - eye.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        return (float) (-Math.toDegrees(Math.atan2(dy, flat)));
    }

    /**
     * Moves one tick's worth toward the given angles. Returns true once the player is
     * within {@code tolerance} degrees of both, i.e. close enough to act.
     */
    public static boolean step(LocalPlayer player, float wantYaw, float wantPitch, float tolerance) {
        float yawDiff = Mth.wrapDegrees(wantYaw - player.getYRot());
        float pitchDiff = Mth.wrapDegrees(wantPitch - player.getXRot());

        boolean aimed = Math.abs(yawDiff) <= tolerance && Math.abs(pitchDiff) <= tolerance;
        if (aimed) return true;

        float yawStep = Mth.clamp(yawDiff * EASE, -MAX_STEP, MAX_STEP);
        float pitchStep = Mth.clamp(pitchDiff * EASE, -MAX_STEP, MAX_STEP);

        // Only jitter while there is real distance left; otherwise the camera would
        // twitch forever instead of settling.
        if (Math.abs(yawDiff) > 3f) {
            yawStep += (float) (RANDOM.nextGaussian() * 0.35);
            pitchStep += (float) (RANDOM.nextGaussian() * 0.2);
        }

        player.setYRot(player.getYRot() + yawStep);
        player.setXRot(Mth.clamp(player.getXRot() + pitchStep, -90f, 90f));
        return false;
    }

    /** Convenience: aim at a point, returning true once on target. */
    public static boolean lookAt(LocalPlayer player, Vec3 point, float tolerance) {
        return step(player, yawTo(player, point), pitchTo(player, point), tolerance);
    }

    /** A randomised tick count, so repeated actions don't share one cadence. */
    public static int jitterTicks(int base, int spread) {
        return base + RANDOM.nextInt(Math.max(1, spread));
    }
}
