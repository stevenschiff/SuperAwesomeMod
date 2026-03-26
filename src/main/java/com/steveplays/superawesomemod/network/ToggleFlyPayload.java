package com.steveplays.superawesomemod.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

/**
 * Client-to-server packet that asks the server to toggle the player's fly ability.
 * Carries no data — the server determines the new state by flipping the current one.
 */
public record ToggleFlyPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleFlyPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("superawesomemod", "toggle_fly"));

    public static final StreamCodec<ByteBuf, ToggleFlyPayload> CODEC =
        StreamCodec.unit(new ToggleFlyPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
