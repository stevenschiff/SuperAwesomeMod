package com.steveplays.superawesomemod;

public final class BodyTwistData {

    /** Vanilla's limit, from LivingEntity.getMaxHeadRotationRelativeToBody(). */
    public static final int VANILLA_LIMIT = 50;
    public static final int MIN_LIMIT = 50;
    /** 180 means the torso may face fully backwards over the legs. */
    public static final int MAX_LIMIT = 180;

    private static boolean enabled = false;
    private static int twistLimit = 120;

    private BodyTwistData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getTwistLimit()      { return twistLimit; }
    public static void setTwistLimit(int t) { twistLimit = Math.clamp(t, MIN_LIMIT, MAX_LIMIT); }

    public static boolean isUnlimited() {
        return twistLimit >= MAX_LIMIT;
    }
}
