package com.steveplays.superawesomemod.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

/**
 * Client-to-server packet that carries a new jump height multiplier.
 * Sent by the GUI; handled server-side to update PlayerJumpData.
 * Uses Fabric networking — not subject to command permission checks.
 */
public record JumpHeightPayload(float multiplier) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JumpHeightPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("superawesomemod", "jump_height"));

    public static final StreamCodec<ByteBuf, JumpHeightPayload> CODEC =
        ByteBufCodecs.FLOAT.map(JumpHeightPayload::new, JumpHeightPayload::multiplier);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
