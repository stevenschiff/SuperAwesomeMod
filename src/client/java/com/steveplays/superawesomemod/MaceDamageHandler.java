package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;

/**
 * Keeps the server's fall distance alive so a mace smash lands from standing.
 *
 * <p>The mace's bonus is computed entirely from the <em>server's</em>
 * {@code fallDistance} for us, inside {@code MaceItem.getAttackDamageBonus}. The
 * server builds that number from the positions and the onGround flag we report, and
 * it throws it away in exactly two places: when a movement packet says onGround, and
 * when the reported Y rises above the last accepted one.
 *
 * <p>So this holds onGround false while a mace is in hand. Fall distance earned from
 * any real drop then survives walking around instead of being wiped on landing, and
 * every mace hit smashes with it. What it cannot do is invent height — resting on a
 * block and reporting a descent is rejected by the server's own collision check, so
 * the number still has to be earned by falling once.
 *
 * <p>Turning it off has to be careful: the server has been holding a live fall
 * distance the whole time, and the first honest onGround would cash it in as fall
 * damage. So disabling first sends the upward nudge that makes the server drop it,
 * the same trick {@code FlightFallResetMixin} uses.
 */
public final class MaceDamageHandler {

    /** Our running estimate of the server's fall distance. Display only. */
    private static double estimate = 0.0;
    private static double lastY = Double.NaN;
    private static boolean wasActive = false;

    private MaceDamageHandler() {}

    /** True while we are suppressing onGround for the mace. */
    public static boolean isActive(Minecraft client) {
        if (!MaceDamageData.isEnabled()) return false;
        LocalPlayer player = client.player;
        return player != null && player.getMainHandItem().is(Items.MACE);
    }

    public static double getEstimate() {
        return estimate;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            estimate = 0.0;
            lastY = Double.NaN;
            wasActive = false;
            return;
        }

        boolean active = isActive(client);

        if (wasActive && !active) {
            // Hand the held fall distance back before an honest onGround arrives and
            // charges us for all of it at once.
            clearServerFallDistance(client, player);
            estimate = 0.0;
        }
        wasActive = active;

        double y = player.getY();
        double previous = lastY;
        lastY = y;

        if (!active) {
            estimate = 0.0;
            return;
        }

        if (Double.isNaN(previous)) return;

        if (y < previous) {
            // Descending: the server accumulates the same delta we report.
            estimate += previous - y;
        } else if (y > previous) {
            // Rising past the last accepted Y is one of the two things that makes the
            // server drop the distance, so our estimate has to drop with it.
            estimate = 0.0;
        }
    }

    private static void clearServerFallDistance(Minecraft client, LocalPlayer player) {
        if (client.getConnection() == null) return;
        client.getConnection().send(new ServerboundMovePlayerPacket.Pos(
            player.getX(), player.getY() + 0.001, player.getZ(),
            false, player.horizontalCollision));
    }
}
