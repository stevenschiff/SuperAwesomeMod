package com.steveplays.superawesomemod.mixin;

import com.steveplays.superawesomemod.AutoMineData;
import com.steveplays.superawesomemod.AutoMineHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Walks the player for the auto miner by writing the same movement input the keyboard
 * would produce.
 *
 * <p>Overwriting {@code moveVector} and {@code keyPresses} at the tail of
 * {@code KeyboardInput.tick()} means everything downstream — friction, collision, the
 * position packets the server validates — behaves exactly as if the keys were held. The
 * alternative, nudging the player's position directly, would desync and read as teleporting.
 *
 * <p>The move vector is in the player's local frame, so the heading is rotated into
 * player space; otherwise the walker would stride off in a fixed world direction whenever
 * the camera turned.
 */
@Mixin(KeyboardInput.class)
public abstract class AutoMineInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void superawesomemod$driveAutoMine(CallbackInfo ci) {
        if (!AutoMineData.isRunning()) return;

        BlockPos goal = AutoMineHandler.getMoveTarget();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (goal == null) {
            // Standing still is a state too — mining, or stopped.
            this.moveVector = Vec2.ZERO;
            this.keyPresses = new Input(false, false, false, false, false,
                                        this.keyPresses.shift(), false);
            return;
        }

        double dx = (goal.getX() + 0.5) - player.getX();
        double dz = (goal.getZ() + 0.5) - player.getZ();

        // World heading rotated into the player's local frame.
        float yawRad = (float) Math.toRadians(player.getYRot());
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        double forward = dz * cos + dx * sin;
        double strafe  = dx * cos - dz * sin;

        double len = Math.sqrt(forward * forward + strafe * strafe);
        if (len < 1.0E-4) {
            this.moveVector = Vec2.ZERO;
            return;
        }
        forward /= len;
        strafe /= len;

        this.moveVector = new Vec2((float) strafe, (float) forward);

        boolean jump = AutoMineHandler.wantsJump();
        this.keyPresses = new Input(
            forward > 0.3, forward < -0.3, strafe > 0.3, strafe < -0.3,
            jump, this.keyPresses.shift(), false);
    }
}
