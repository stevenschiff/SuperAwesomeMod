package com.steveplays.superawesomemod;

import com.steveplays.superawesomemod.mixin.ConnectionAccessor;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

/**
 * A no-op {@link Connection} for fake players.
 * <p>
 * Uses an {@link EmbeddedChannel} so that {@code isConnected()} returns
 * {@code true}, but all outgoing packets are silently discarded.
 */
public class FakeClientConnection extends Connection {

    public FakeClientConnection() {
        super(PacketFlow.SERVERBOUND);
        ((ConnectionAccessor) this).setChannel(new EmbeddedChannel());
    }

    @Override
    public void send(Packet<?> packet) {
        // Silently discard — there is no real client.
    }

    @Override
    public void send(Packet<?> packet, ChannelFutureListener listener) {
        // Silently discard.
    }

    @Override
    public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
        // Silently discard.
    }

    @Override
    public void handleDisconnection() {
        // No-op: nothing to clean up.
    }

    @Override
    public void setReadOnly() {
        // No-op: prevent channel state changes.
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener listener) {
        // No-op: no handshake for fake players.
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> info, T listener) {
        // No-op: placeNewPlayer calls this to configure the channel pipeline.
    }
}
