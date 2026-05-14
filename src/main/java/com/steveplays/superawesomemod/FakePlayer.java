package com.steveplays.superawesomemod;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * A server-side fake player that can hold items and continuously use them
 * (e.g. keep a shield raised). Created via the {@code /npc} commands.
 */
public class FakePlayer extends ServerPlayer {

    private boolean continuousUseMainHand = false;
    private boolean continuousUseOffHand = false;

    /** How many ticks to wait before re-raising the shield after it gets disabled. */
    private int shieldDisableDuration = 100; // 5 seconds (vanilla default)
    /** Ticks remaining before the shield can be re-raised. */
    private int shieldDisableTicksRemaining = 0;

    private FakePlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile, ClientInformation.createDefault());
    }

    /**
     * Spawns a fake player at the given position and registers it with the
     * server's {@link net.minecraft.server.players.PlayerList}.
     * <p>
     * Must be called on the server thread (commands run on the server thread).
     */
    public static FakePlayer spawn(MinecraftServer server, ServerLevel level,
                                   String name, double x, double y, double z,
                                   float yaw, float pitch) {

        UUID uuid = UUID.nameUUIDFromBytes(("FakePlayer:" + name).getBytes());
        GameProfile profile = new GameProfile(uuid, name);

        FakePlayer fp = new FakePlayer(server, level, profile);
        fp.snapTo(x, y, z, yaw, pitch);

        FakeClientConnection fakeConn = new FakeClientConnection();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        server.getPlayerList().placeNewPlayer(fakeConn, fp, cookie);

        fp.connection.resetPosition();
        return fp;
    }

    // ------------------------------------------------------------------
    // Continuous item use
    // ------------------------------------------------------------------

    public void setContinuousUse(InteractionHand hand, boolean enabled) {
        if (hand == InteractionHand.MAIN_HAND) {
            continuousUseMainHand = enabled;
        } else {
            continuousUseOffHand = enabled;
        }
        if (!enabled && isUsingItem() && getUsedItemHand() == hand) {
            stopUsingItem();
        }
    }

    public boolean isContinuousUse(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? continuousUseMainHand : continuousUseOffHand;
    }

    // ------------------------------------------------------------------
    // Shield disable delay
    // ------------------------------------------------------------------

    public void setShieldDisableDuration(int ticks) {
        this.shieldDisableDuration = ticks;
    }

    public int getShieldDisableDuration() {
        return shieldDisableDuration;
    }

    @Override
    public void stopUsingItem() {
        // If continuous use is still enabled for this hand, the stop was caused
        // externally (e.g. axe hit disabling the shield), so start the cooldown.
        if (isUsingItem()) {
            InteractionHand hand = getUsedItemHand();
            if (isContinuousUse(hand)) {
                shieldDisableTicksRemaining = shieldDisableDuration;
            }
        }
        super.stopUsingItem();
    }

    // ------------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        MinecraftServer srv = this.level().getServer();
        if (srv != null && srv.getTickCount() % 10 == 0) {
            this.connection.resetPosition();
            this.level().getChunkSource().move(this);
        }

        try {
            super.tick();
            this.doTick();
        } catch (NullPointerException ignored) {
            // Defensive: some code paths may touch connection state
            // that doesn't fully exist for fake players.
        }

        tickContinuousItemUse();
    }

    private void tickContinuousItemUse() {
        if (shieldDisableTicksRemaining > 0) {
            shieldDisableTicksRemaining--;
            return;
        }
        if (continuousUseMainHand) {
            ItemStack main = getItemInHand(InteractionHand.MAIN_HAND);
            if (!main.isEmpty() && !isUsingItem()) {
                startUsingItem(InteractionHand.MAIN_HAND);
            }
        }
        if (continuousUseOffHand && !isUsingItem()) {
            ItemStack off = getItemInHand(InteractionHand.OFF_HAND);
            if (!off.isEmpty()) {
                startUsingItem(InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack stack) {
        // Suppress equipment-change processing while using an item to
        // prevent the use state from being interrupted by entity data sync.
        if (!isUsingItem()) {
            super.onEquipItem(slot, previous, stack);
        }
    }

    @Override
    public void disconnect() {
        // Intentionally empty — prevents ServerCommonPacketListenerImpl.onDisconnect()
        // from firing (which in singleplayer could halt the server).
        // FakePlayerManager handles removal via PlayerList.remove() instead.
    }
}
