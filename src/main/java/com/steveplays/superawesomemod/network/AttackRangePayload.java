package com.steveplays.superawesomemod.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

/**
 * Client-to-server packet that sets the player's entity interaction (attack) range.
 */
public record AttackRangePayload(float range) implements CustomPacketPayload {

    public static final float DEFAULT = 3.0f;  // vanilla survival default
    public static final float MIN     = 1.0f;
    public static final float MAX     = 20.0f;

    public static final CustomPacketPayload.Type<AttackRangePayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("superawesomemod", "attack_range"));

    public static final StreamCodec<ByteBuf, AttackRangePayload> CODEC =
        ByteBufCodecs.FLOAT.map(AttackRangePayload::new, AttackRangePayload::range);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
