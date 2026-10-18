package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Turns the player toward a point the way a hand would, not the way a script would.
 *
 * <p>Snapping yaw straight to the target is the single clearest tell that nobody is
 * holding the mouse, so the turn is eased: a fraction of what is left is covered each
 * step, capped at a maximum rate, which gives a fast start and a soft finish.
 *
 * <p>The path to the target is a straight one. Random noise on the angles was tried and
 * removed: re-rolled per step it reads as a shaking hand rather than a steady one, and a
 * smooth arc is both nicer to watch and more accurate. The only randomness left is the
 * aim point inside the block, which is chosen once per block and then held, so the turn
 * toward it is still direct.
 *
 * <p>The easing runs <b>once per rendered frame</b>, not once per tick. A tick is 50 ms,
 * so tick-rate easing moves the camera in 20 visible jumps a second however high the
 * frame rate is. {@link #renderStep} is driven from the renderer and scales each step by
 * real elapsed time, so the same turn takes the same wall-clock duration at any frame
 * rate — it just gets subdivided more finely when there are more frames to fill it.
 *
 * <p>The mining state machine only calls {@link #aimAt} to say where it wants to look;
 * it never moves the camera itself. That split is what lets the motion be smooth: if the
 * tick path also wrote rotation, the two would fight and the result would stutter.
 *
 * <p>Rotation is applied to the player's real yaw and pitch, so it travels to the server
 * in ordinary movement packets. Those are still only sent per tick, so this is a visual
 * change — the server sees the same turn either way.
 */
public final class RotationHelper {

    /** Hard ceiling on turn rate, in degrees per second. */
    private static final float MAX_RATE = 440f;
    /** Fraction of the remaining angle covered per tick, before the cap applies. */
    private static final float EASE_PER_TICK = 0.35f;
    /** Ticks per second, for converting the per-tick ease into a per-second rate. */
    private static final float TPS = 20f;

    private static final Random RANDOM = new Random();

    /** Where the state machine wants to look, or null when nothing is steering. */
    private static Float wantYaw = null;
    private static Float wantPitch = null;
    private static long lastFrameNanos = 0L;

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
     * Asks for these angles and reports whether the player is already within
     * {@code tolerance} degrees of both. The turn itself happens in {@link #renderStep}.
     */
    public static boolean aimAt(LocalPlayer player, float yaw, float pitch, float tolerance) {
        float yawDiff = Mth.wrapDegrees(yaw - player.getYRot());
        float pitchDiff = Mth.wrapDegrees(pitch - player.getXRot());

        wantYaw = yaw;
        wantPitch = pitch;
        return Math.abs(yawDiff) <= tolerance && Math.abs(pitchDiff) <= tolerance;
    }

    /** Convenience: aim at a point, returning true once on target. */
    public static boolean lookAt(LocalPlayer player, Vec3 point, float tolerance) {
        return aimAt(player, yawTo(player, point), pitchTo(player, point), tolerance);
    }

    /** Hands the mouse back, so nothing keeps nudging the camera once mining stops. */
    public static void release() {
        wantYaw = null;
        wantPitch = null;
    }

    public static boolean isSteering() {
        return wantYaw != null;
    }

    /**
     * One frame's worth of turn. Called from the renderer before the camera is set up, so
     * the rotation written here is the rotation drawn this frame.
     */
    public static void renderStep(Minecraft client) {
        long now = System.nanoTime();
        long previous = lastFrameNanos;
        lastFrameNanos = now;

        if (wantYaw == null || wantPitch == null) return;
        if (client.player == null || client.screen != null) return;
        LocalPlayer player = client.player;

        // First frame after taking over has no previous timestamp to measure against.
        if (previous == 0L) return;
        // Clamp so a stutter or a paused window can't produce one enormous jump.
        float dt = Math.clamp((now - previous) / 1_000_000_000f, 0f, 0.1f);
        if (dt <= 0f) return;

        float yawDiff = Mth.wrapDegrees(wantYaw - player.getYRot());
        float pitchDiff = Mth.wrapDegrees(wantPitch - player.getXRot());

        // Exponential approach expressed per second, so the curve matches the old
        // per-tick feel at 20 fps and simply gets finer above it.
        float factor = 1f - (float) Math.pow(1f - EASE_PER_TICK, dt * TPS);
        float cap = MAX_RATE * dt;

        float yawStep = Mth.clamp(yawDiff * factor, -cap, cap);
        float pitchStep = Mth.clamp(pitchDiff * factor, -cap, cap);

        apply(player, yawStep, pitchStep);
    }

    /**
     * Writes the step the way mouse input does: the previous-frame angles move with the
     * current ones, so the camera tracks the turn directly instead of lagging a frame
     * behind through vanilla's render interpolation.
     */
    private static void apply(LocalPlayer player, float yawStep, float pitchStep) {
        player.setYRot(player.getYRot() + yawStep);
        player.yRotO += yawStep;

        player.setXRot(Mth.clamp(player.getXRot() + pitchStep, -90f, 90f));
        player.xRotO = Mth.clamp(player.xRotO + pitchStep, -90f, 90f);
    }

    /** A randomised tick count, so repeated actions don't share one cadence. */
    public static int jitterTicks(int base, int spread) {
        return base + RANDOM.nextInt(Math.max(1, spread));
    }
}
