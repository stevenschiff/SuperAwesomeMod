package com.steveplays.superawesomemod;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reach maths shared by the spacing trainer and the full-colour hitbox tint.
 *
 * <p>Deliberately the same quantity the server validates against: {@code Player}
 * checks {@code aABB.distanceToSqr(getEyePosition())} against the entity interaction
 * range, so "gap" here means eye to the nearest point of the hitbox — 0 when you are
 * touching, and never above the reach when a hit actually connects.
 *
 * <p>{@code CombatHitboxRenderer} carries its own private copy of this arithmetic. It
 * is left alone rather than refactored so a working feature isn't disturbed; the two
 * must stay in agreement, since Full Colour is a sub-toggle of that renderer.
 */
public final class CombatReach {

    private CombatReach() {}

    /** The player's attack reach, honouring any attribute modifiers (spears raise it). */
    public static double attackReach(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        return attr != null ? attr.getValue() : Player.DEFAULT_ENTITY_INTERACTION_RANGE;
    }

    /** Distance from the eye to the nearest point of the box; 0 when inside it. */
    public static double gap(Vec3 eye, AABB box) {
        return Math.sqrt(gapSqr(eye, box));
    }

    public static double gapSqr(Vec3 eye, AABB box) {
        double dx = Math.max(0.0, Math.max(box.minX - eye.x, eye.x - box.maxX));
        double dy = Math.max(0.0, Math.max(box.minY - eye.y, eye.y - box.maxY));
        double dz = Math.max(0.0, Math.max(box.minZ - eye.z, eye.z - box.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }
}
