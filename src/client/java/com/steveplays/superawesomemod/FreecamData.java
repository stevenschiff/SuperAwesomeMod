package com.steveplays.superawesomemod;

import net.minecraft.util.Mth;

public final class FreecamData {

    // Per-tick movement speeds in blocks. Tuned to feel similar to vanilla flight presets.
    public static final float SLOW      = 0.15f;
    public static final float NORMAL    = 0.40f;
    public static final float FAST      = 1.00f;
    public static final float VERY_FAST = 2.00f;

    private static boolean enabled = false;
    private static double  x, y, z;
    private static double  prevX, prevY, prevZ;
    private static float   yaw, pitch;
    private static float   speed = NORMAL;

    // Velocity smoothing — camera accelerates/decelerates instead of moving instantly.
    private static double velX, velY, velZ;
    private static final double SMOOTHING = 0.3;
    private static final double VELOCITY_CUTOFF = 0.001;

    private FreecamData() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean e) { enabled = e; }

    public static double getX() { return x; }
    public static double getY() { return y; }
    public static double getZ() { return z; }

    public static void setPos(double nx, double ny, double nz) {
        // Seed prev to match current so the first interpolated frame doesn't lerp
        // in from a stale position when freecam (re)starts.
        prevX = nx; prevY = ny; prevZ = nz;
        x = nx; y = ny; z = nz;
        resetVelocity();
    }

    public static void translate(double dx, double dy, double dz) {
        x += dx; y += dy; z += dz;
    }

    /** Smoothly accelerate/decelerate towards the target velocity each tick. */
    public static void smoothMove(double targetDx, double targetDy, double targetDz) {
        velX += (targetDx - velX) * SMOOTHING;
        velY += (targetDy - velY) * SMOOTHING;
        velZ += (targetDz - velZ) * SMOOTHING;
        if (Math.abs(velX) < VELOCITY_CUTOFF) velX = 0;
        if (Math.abs(velY) < VELOCITY_CUTOFF) velY = 0;
        if (Math.abs(velZ) < VELOCITY_CUTOFF) velZ = 0;
        x += velX; y += velY; z += velZ;
    }

    public static void resetVelocity() {
        velX = velY = velZ = 0;
    }

    // Snapshots current position into prev. Call once per client tick BEFORE input
    // is applied so render-time interpolation has a stable origin to lerp from.
    public static void beginTick() {
        prevX = x; prevY = y; prevZ = z;
    }

    public static double getInterpolatedX(float partialTick) { return Mth.lerp(partialTick, prevX, x); }
    public static double getInterpolatedY(float partialTick) { return Mth.lerp(partialTick, prevY, y); }
    public static double getInterpolatedZ(float partialTick) { return Mth.lerp(partialTick, prevZ, z); }

    public static float getYaw()   { return yaw; }
    public static float getPitch() { return pitch; }

    public static void setRot(float y, float p) {
        yaw = y;
        pitch = (float) Math.clamp(p, -90f, 90f);
    }

    public static void addYaw(float dy)   { yaw += dy; }
    public static void addPitch(float dp) { pitch = (float) Math.clamp(pitch + dp, -90f, 90f); }

    public static float getSpeed()           { return speed; }
    public static void  setSpeed(float s)    { speed = s; }
}
