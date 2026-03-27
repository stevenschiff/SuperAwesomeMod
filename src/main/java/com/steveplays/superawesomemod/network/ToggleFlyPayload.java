package com.steveplays.superawesomemod.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

/**
 * Client-to-server packet that explicitly sets the player's fly ability state.
 * Carries the intended enabled/disabled state to avoid toggle-race issues.
 */
public record ToggleFlyPayload(boolean enabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleFlyPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("superawesomemod", "toggle_fly"));

    public static final StreamCodec<ByteBuf, ToggleFlyPayload> CODEC =
        ByteBufCodecs.BOOL.map(ToggleFlyPayload::new, ToggleFlyPayload::enabled);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
