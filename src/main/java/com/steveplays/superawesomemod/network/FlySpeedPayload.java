package com.steveplays.superawesomemod.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

/**
 * Client-to-server packet that sets the player's flying speed.
 */
public record FlySpeedPayload(float speed) implements CustomPacketPayload {

    // Creative default is 0.05f. Values are intentionally small — the engine
    // multiplies this internally so even 0.2f feels very fast.
    public static final float SLOW      = 0.02f;
    public static final float NORMAL    = 0.05f;  // creative default
    public static final float FAST      = 0.1f;
    public static final float VERY_FAST = 0.2f;

    public static final CustomPacketPayload.Type<FlySpeedPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("superawesomemod", "fly_speed"));

    public static final StreamCodec<ByteBuf, FlySpeedPayload> CODEC =
        ByteBufCodecs.FLOAT.map(FlySpeedPayload::new, FlySpeedPayload::speed);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
