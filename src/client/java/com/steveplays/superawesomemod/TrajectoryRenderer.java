package com.steveplays.superawesomemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Trajectory Preview: draws where the thing in your hand will land.
 *
 * <p>Steps the same maths the real projectile will use — the launch direction from
 * {@code Projectile.shootFromRotation}, then per tick {@code pos += velocity},
 * {@code velocity *= drag}, {@code velocity.y -= gravity} — with the constants read
 * out of the 1.21.11 classes rather than guessed, so the arc matches the shot
 * instead of merely resembling one. Purely local: nothing is sent, nothing changes.
 */
public final class TrajectoryRenderer {

    private static final int MAX_STEPS = 300;
    private static final float AIR_DRAG = 0.99F;

    private TrajectoryRenderer() {}

    /** Launch parameters for one kind of throwable. */
    private record Launch(float power, double gravity, float pitchOffset) {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext ctx) {
        if (!TrajectoryData.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Launch launch = launchFor(player);
        if (launch == null || launch.power() <= 0.0F) return;

        List<Vec3> path = new ArrayList<>();
        Vec3 impact = simulate(player, level, launch, path);
        if (path.size() < 2) return;

        Camera camera = ctx.gameRenderer().getMainCamera();
        Vec3 camPos = camera.position();
        PoseStack.Pose pose = ctx.matrices().last();
        Matrix4f matrix = pose.pose();
        VertexConsumer buf = consumers.getBuffer(XrayLineRenderType.LINES_XRAY);

        for (int i = 0; i < path.size() - 1; i++) {
            line(buf, matrix, pose, path.get(i), path.get(i + 1), camPos, 1.0F, 1.0F, 1.0F);
        }

        if (impact != null) {
            drawMarker(buf, matrix, pose, impact, camPos);
        }
    }

    /** Which projectile the held items describe, or null if none of them throw anything. */
    private static Launch launchFor(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        Launch fromMain = launchFor(player, main);
        return fromMain != null ? fromMain : launchFor(player, off);
    }

    private static Launch launchFor(LocalPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.is(Items.BOW)) {
            // While drawing, preview the shot we'd actually get; at rest, preview a
            // full-power shot rather than a zero-length stub.
            float power = player.isUsingItem() && player.getUseItem() == stack
                ? BowItem.getPowerForTime(player.getTicksUsingItem()) * 3.0F
                : 3.0F;
            return new Launch(power, 0.05, 0.0F);
        }
        if (stack.is(Items.CROSSBOW))                     return new Launch(3.15F, 0.05, 0.0F);
        if (stack.is(Items.TRIDENT))                      return new Launch(2.5F,  0.05, 0.0F);
        if (stack.is(Items.ENDER_PEARL)
            || stack.is(Items.SNOWBALL)
            || stack.is(Items.EGG))                       return new Launch(1.5F,  0.03, 0.0F);
        if (stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION))          return new Launch(0.5F,  0.05, -20.0F);

        return null;
    }

    /**
     * Walks the projectile forward, filling {@code path}. Returns the impact point,
     * or null if it never hit anything within the step budget.
     */
    private static Vec3 simulate(LocalPlayer player, ClientLevel level, Launch launch, List<Vec3> path) {
        float xRot = player.getXRot() + launch.pitchOffset();
        float yRot = player.getYRot();

        // Same direction maths as Projectile.shootFromRotation.
        float dx = -Mth.sin(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));
        float dy = -Mth.sin(xRot * (float) (Math.PI / 180.0));
        float dz =  Mth.cos(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));

        Vec3 velocity = new Vec3(dx, dy, dz).normalize().scale(launch.power());

        // Projectiles inherit the shooter's motion, vertical only when airborne.
        Vec3 shooter = player.getKnownMovement();
        velocity = velocity.add(shooter.x, player.onGround() ? 0.0 : shooter.y, shooter.z);

        Vec3 pos = player.getEyePosition().subtract(0.0, 0.1, 0.0);
        path.add(pos);

        for (int step = 0; step < MAX_STEPS; step++) {
            Vec3 next = pos.add(velocity);

            BlockHitResult blockHit = level.clip(new ClipContext(
                pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (blockHit.getType() != HitResult.Type.MISS) {
                path.add(blockHit.getLocation());
                return blockHit.getLocation();
            }

            Vec3 entityHit = clipEntities(player, level, pos, next);
            if (entityHit != null) {
                path.add(entityHit);
                return entityHit;
            }

            pos = next;
            path.add(pos);

            if (pos.y < level.getMinY() - 8) return null;

            velocity = velocity.scale(AIR_DRAG);
            velocity = velocity.subtract(0.0, launch.gravity(), 0.0);
        }
        return null;
    }

    /** Nearest entity struck by the segment, or null. */
    private static Vec3 clipEntities(LocalPlayer player, ClientLevel level, Vec3 from, Vec3 to) {
        AABB sweep = new AABB(from, to).inflate(1.0);
        Vec3 best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : level.getEntities(player, sweep, e -> !e.isSpectator() && e.isPickable())) {
            AABB box = entity.getBoundingBox().inflate(0.3);
            Vec3 hit = box.clip(from, to).orElse(null);
            if (hit == null) continue;

            double dist = from.distanceToSqr(hit);
            if (dist < bestDist) {
                bestDist = dist;
                best = hit;
            }
        }
        return best;
    }

    private static void drawMarker(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                                   Vec3 at, Vec3 camPos) {
        double s = 0.25;
        line(buf, mat, pose, at.add(-s, 0, 0), at.add(s, 0, 0), camPos, 1.0F, 0.3F, 0.3F);
        line(buf, mat, pose, at.add(0, -s, 0), at.add(0, s, 0), camPos, 1.0F, 0.3F, 0.3F);
        line(buf, mat, pose, at.add(0, 0, -s), at.add(0, 0, s), camPos, 1.0F, 0.3F, 0.3F);
    }

    private static void line(VertexConsumer buf, Matrix4f mat, PoseStack.Pose pose,
                             Vec3 from, Vec3 to, Vec3 camPos, float r, float g, float b) {
        float x1 = (float) (from.x - camPos.x);
        float y1 = (float) (from.y - camPos.y);
        float z1 = (float) (from.z - camPos.z);
        float x2 = (float) (to.x - camPos.x);
        float y2 = (float) (to.y - camPos.y);
        float z2 = (float) (to.z - camPos.z);

        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-5F) return;
        nx /= len; ny /= len; nz /= len;

        buf.addVertex(mat, x1, y1, z1).setColor(r, g, b, 1.0F).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
        buf.addVertex(mat, x2, y2, z2).setColor(r, g, b, 1.0F).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
    }
}
