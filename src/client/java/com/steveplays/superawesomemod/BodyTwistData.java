package com.steveplays.superawesomemod;

public final class BodyTwistData {

    /** Vanilla's own limit, from LivingEntity.getMaxHeadRotationRelativeToBody(). */
    public static final int VANILLA_LIMIT = 50;
    /** Past this the legacy formula starts easing the legs around. */
    public static final int EASE_ABOVE = 50;

    public static final int MIN_LEAD = 50;
    public static final int MAX_LEAD = 120;

    private static boolean enabled = false;
    /** How far the upper body may lead the legs. Animatium uses a fixed 75. */
    private static int leadLimit = 75;
    private static boolean backwardsWalking = true;
    private static boolean snapHeadRotation = false;

    private BodyTwistData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getLeadLimit()      { return leadLimit; }
    public static void setLeadLimit(int l) { leadLimit = Math.clamp(l, MIN_LEAD, MAX_LEAD); }

    /** Stop the body flipping 180 degrees when walking backwards. */
    public static boolean isBackwardsWalking()           { return backwardsWalking; }
    public static void    setBackwardsWalking(boolean v) { backwardsWalking = v; }

    /** Other players' heads snap instead of easing, the way they did in 1.7. */
    public static boolean isSnapHeadRotation()           { return snapHeadRotation; }
    public static void    setSnapHeadRotation(boolean v) { snapHeadRotation = v; }
}
