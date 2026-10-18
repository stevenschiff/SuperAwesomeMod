package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.FishingHookAccessor;
import com.steveplays.superawesomemod.mixin.MinecraftUseInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Casts, reels on a bite, and casts again.
 *
 * <p>The bite is read from the hook's synced flag rather than guessed from the splash
 * sound or the bobber dipping, so the reel lands on the actual catch window instead of a
 * lookalike — rain and nearby splashes both fool the audio approach.
 *
 * <p>Every wait is randomised. A reel that happens exactly N ticks after every bite, for
 * hours, is a clearer signal than any single action.
 */
public final class AutoFishHandler {

    private enum State { IDLE, CAST_WAIT, FISHING, REEL_WAIT }

    private static State state = State.IDLE;
    private static int delay = 0;

    private AutoFishHandler() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (!AutoFishData.isEnabled() || player == null || client.screen != null) {
            state = State.IDLE;
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!held.is(Items.FISHING_ROD)) {
            AutoFishData.setStatus("No rod in hand");
            state = State.IDLE;
            return;
        }

        // Don't fish a good rod to death; Mending and Luck of the Sea are worth more
        // than the fish.
        int left = held.getMaxDamage() - held.getDamageValue();
        if (held.isDamageableItem() && left <= AutoFishData.getDurabilityReserve()) {
            AutoFishData.setStatus("Stopped: rod nearly broken");
            state = State.IDLE;
            return;
        }

        if (delay > 0) {
            delay--;
            return;
        }

        FishingHook hook = player.fishing;

        switch (state) {
            case IDLE, CAST_WAIT -> {
                if (hook == null) {
                    AutoFishData.setStatus("Casting");
                    use(client);
                    delay = RotationHelper.jitterTicks(8, 8);
                    state = State.CAST_WAIT;
                } else {
                    AutoFishData.setStatus("Fishing");
                    state = State.FISHING;
                }
            }
            case FISHING -> {
                if (hook == null) {
                    state = State.IDLE;
                    return;
                }
                if (isBiting(hook)) {
                    AutoFishData.setStatus("Reeling in");
                    // A person does not react instantly to the splash.
                    delay = RotationHelper.jitterTicks(3, 5);
                    state = State.REEL_WAIT;
                }
            }
            case REEL_WAIT -> {
                use(client);
                AutoFishData.setStatus("Recasting");
                delay = RotationHelper.jitterTicks(12, 12);
                state = State.IDLE;
            }
        }
    }

    private static boolean isBiting(FishingHook hook) {
        return hook.getEntityData().get(FishingHookAccessor.superawesomemod$dataBiting());
    }

    private static void use(Minecraft client) {
        ((MinecraftUseInvoker) (Object) client).superawesomemod$startUseItem();
    }
}
