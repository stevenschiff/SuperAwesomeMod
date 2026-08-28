package com.steveplays.superawesomemod;

public final class MaceDamageData {

    /** Vanilla's smash gate: MaceItem.canSmashAttack needs more than this. */
    public static final double SMASH_THRESHOLD = 1.5;

    private static boolean enabled = false;

    private MaceDamageData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    /**
     * Bonus damage a smash would deal at a given fall distance, straight out of
     * {@code MaceItem.getAttackDamageBonus}:
     *
     * <pre>
     * fd <= 3  ->  4 * fd
     * fd <= 8  ->  12 + 2 * (fd - 3)
     * else     ->  22 + (fd - 8)
     * </pre>
     *
     * Enchantment (Density) bonuses are server-side and not modelled here, so this
     * reads slightly low on an enchanted mace.
     */
    public static double bonusDamageAt(double fallDistance) {
        if (fallDistance <= SMASH_THRESHOLD) return 0.0;
        if (fallDistance <= 3.0) return 4.0 * fallDistance;
        if (fallDistance <= 8.0) return 12.0 + 2.0 * (fallDistance - 3.0);
        return 22.0 + (fallDistance - 8.0);
    }
}
