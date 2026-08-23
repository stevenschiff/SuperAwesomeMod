package com.steveplays.superawesomemod;

/**
 * Marks the window in which the first-person shield is being drawn.
 *
 * <p>{@code ShieldSpecialRenderer} is shared by every shield in the world — the one in
 * your hand, the one on the player next to you, the one in an item frame. It receives
 * an {@code ItemDisplayContext} but the tidiest way to scope our changes to our own
 * hand is to flag the window from {@code ItemInHandRenderer}, which is unambiguously
 * first person. Safe as a plain static because the chain from there down to the shield
 * renderer is synchronous and rendering happens on one thread.
 */
public final class ShieldRenderFlag {

    private static boolean firstPersonShield = false;

    private ShieldRenderFlag() {}

    public static void set(boolean v)   { firstPersonShield = v; }
    public static boolean isActive()    { return firstPersonShield; }
}
